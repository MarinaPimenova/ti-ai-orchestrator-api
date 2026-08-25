package com.wk.ti.orchestrator.service;

import com.wk.ti.exception.model.ClientErrorResponse;
import com.wk.ti.orchestrator.model.AIGenerativeResponse;
import com.wk.ti.orchestrator.model.QuestionStatus;
import com.wk.ti.orchestrator.model.SubscriptionIdentifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseService implements DisposableBean {
    /**
     * Interval between keep-alive ping events sent to the client (must be < nginx proxy_read_timeout).
     */
    static final int HEARTBEAT_INTERVAL_SECONDS = 30;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>();
    @Value("${app.sse.timeout}")
    private int sseTimeoutMinutes;

    private final QuestionOrchestrator questionOrchestrator;

    private final ThreadPoolTaskExecutor sseTaskExecutor;
    private final TaskScheduler sseHeartbeatScheduler;


    public SseEmitter registerClient(String conversationId, Long questionId) {
        String streamId = getStreamId(conversationId, questionId);
        long overallSseTimeout = (sseTimeoutMinutes * 60 * 1000L) + 30 * 1000L;
        SseEmitter emitter = new SseEmitter(overallSseTimeout);
        emitters.put(streamId, emitter);
        // Register lifecycle callbacks BEFORE the initial send so they are always in place.
        emitter.onCompletion(() -> cleanupEmitter(streamId));
        emitter.onTimeout(() -> cleanupEmitter(streamId));
        emitter.onError(e -> cleanupEmitter(streamId));

        // Send an initial named ping event immediately.  This causes Spring MVC to commit
        // the HTTP 200 / text-event-stream response headers right away so that nginx (and
        // any other proxy) does not hit its "waiting for upstream headers" timeout (typically
        // 60 s) before the real AI response arrives.
        // The named event ("ping") is intentionally NOT the default "message" type, so the
        // browser EventSource.onmessage handler will never try to JSON-parse it.
        try {
            log.debug("Send ping");
            emitter.send(SseEmitter.event().name("ping").data("1"));
        } catch (IOException ex) {
            log.warn("Failed to send initial ping for streamId={}, cleaning up", streamId, ex);
            cleanupEmitter(streamId);
            emitter.completeWithError(ex);
        }
        return emitter;
    }

    private void cleanupEmitter(String streamId) {
        cancelHeartbeat(streamId);
        emitters.remove(streamId);
    }

    private static String getStreamId(String conversationId, Long questionId) {
        return conversationId + "_" + questionId.toString();
    }

    public void cancelAll(List<SubscriptionIdentifier> subscriptions) {
        log.info("Processing bulk cancellation request for {} subscription(s)", subscriptions.size());

        for (SubscriptionIdentifier subscription : subscriptions) {
            try {
                cancel(subscription.conversationId(), subscription.questionId());
                log.debug("Successfully canceled subscription: conversationId={}, questionId={}",
                        subscription.conversationId(), subscription.questionId());
            } catch (Exception e) {
                log.error("Unexpected error during cancellation: conversationId={}, questionId={}, type={}, error={}",
                        subscription.conversationId(), subscription.questionId(), e.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        log.info("Bulk cancellation completed.");
    }


    public void cancel(String conversationId, Long questionId) {
        questionOrchestrator.updateQuestionStatus(questionId, QuestionStatus.CANCELED);
        String streamId = getStreamId(conversationId, questionId);
        SseEmitter emitter = emitters.get(streamId);
        cleanupEmitter(streamId);
        if (emitter != null) {
            emitter.complete();
        } else {
            log.warn("No SSE emitter found for cancellation: streamId={}", streamId);
        }
    }


    public void startGetAnswerProcessing(String conversationId, Long questionId) {
        String streamId = getStreamId(conversationId, questionId);
        // Start a periodic heartbeat so that nginx (proxy_read_timeout, default 60 s) does not
        // close the connection while the AI pipeline is running.  The task is automatically
        // stopped when cleanupEmitter() is called (on completion, timeout, or error).
        startHeartbeat(streamId);
        //SecurityContext securityContext = SecurityContextHolder.getContext();
        CompletableFuture
                .supplyAsync(() -> {
                    //SecurityContext originalContext = SecurityContextHolder.getContext();
                    try {
                        //SecurityContextHolder.setContext(securityContext);
                        return questionOrchestrator.getChatResponse(conversationId, questionId);
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    } finally {
                        log.info("SSE completes");
                        //SecurityContextHolder.setContext(originalContext);
                    }
                }, sseTaskExecutor)
                .completeOnTimeout(null, sseTimeoutMinutes, TimeUnit.MINUTES)
                .thenAccept(response -> {
                    if (response != null) {
                        log.info("Response is received and retuning to user conversationId={} questionId={}",
                                conversationId, questionId);
                        //sendResultToClient(conversationId, questionId, Optional.of(response));
                        resultToClient(conversationId, questionId, Optional.of(response));
                    } else {
                        log.info("Response is null and get timeout conversationId={} questionId={}",
                                conversationId, questionId);
                        handleTimeout(conversationId, questionId);
                    }
                })
                .exceptionally(ex -> {
                    handleFailure(conversationId, questionId, ex);
                    return null;
                });
    }

    // -------------------------------------------------------------------------
    // Heartbeat helpers
    // -------------------------------------------------------------------------
    private void startHeartbeat(String streamId) {
        // Use an explicit startTime so the first heartbeat fires after HEARTBEAT_INTERVAL_SECONDS,
        // not immediately.  scheduleWithFixedDelay(Runnable, Duration) would fire at t=0 first,
        // then every HEARTBEAT_INTERVAL_SECONDS; by supplying Instant.now().plusSeconds(...) we
        // delay the first execution, avoiding a redundant ping right after the initial one.
        ScheduledFuture<?> task = sseHeartbeatScheduler.scheduleWithFixedDelay(
                () -> sendHeartbeat(streamId),
                Instant.now().plusSeconds(HEARTBEAT_INTERVAL_SECONDS),
                Duration.ofSeconds(HEARTBEAT_INTERVAL_SECONDS));
        heartbeatTasks.put(streamId, task);
        log.debug("SSE heartbeat started for streamId={}", streamId);
    }

    private void sendHeartbeat(String streamId) {
        // Capture a local reference first.  Even if cleanupEmitter() removes the emitter from
        // the map between the get() and send() calls, the local reference is still valid and
        // send() will throw IOException which we handle below — no NullPointerException risk.
        SseEmitter emitter = emitters.get(streamId);
        if (emitter == null) {
            // Emitter was already cleaned up; stop the scheduler.
            cancelHeartbeat(streamId);
            return;
        }
        try {
            emitter.send(SseEmitter.event().name("ping").data("1"));
            log.debug("SSE heartbeat sent for streamId={}", streamId);
        } catch (IOException e) {
            log.warn("SSE heartbeat send failed for streamId={} (client likely disconnected)", streamId);
            cancelHeartbeat(streamId);
        }
    }

    private void cancelHeartbeat(String streamId) {
        ScheduledFuture<?> task = heartbeatTasks.remove(streamId);
        if (task != null) {
            task.cancel(false);
            log.debug("SSE heartbeat cancelled for streamId={}", streamId);
        }
    }
    // -------------------------------------------------------------------------
    // Response / error helpers
    // -------------------------------------------------------------------------

    private void handleTimeout(String conversationId, Long questionId) {
        try {
            questionOrchestrator.updateQuestionStatus(questionId, QuestionStatus.TIMED_OUT);
        } catch (Exception e) {
            log.error("Failed to update question status to TIMED_OUT for questionId={}", questionId, e);
        }
        sendExceptionToClient(
                new TimeoutException("Timeout exceeded while retrieving chat response."),
                HttpStatus.REQUEST_TIMEOUT, conversationId, questionId);
    }

    private void handleFailure(String conversationId, Long questionId, Throwable ex) {
        try {
            questionOrchestrator.updateQuestionStatus(questionId, QuestionStatus.FAILED);
        } catch (Exception e) {
            log.error("Failed to update question status to FAILED for questionId={}", questionId, e);
        }
        log.error("Exception in SSE processing for conversationId={}, questionId={}", conversationId, questionId, ex);
        sendExceptionToClient(ex, HttpStatus.INTERNAL_SERVER_ERROR, conversationId, questionId);
    }

    private void sendResultToClient(String conversationId, Long questionId, Optional<AIGenerativeResponse> optionalResponse) {
        ResponseEntity<?> responseEntity;
        if (optionalResponse.isPresent()) {
            responseEntity = ResponseEntity.ok(optionalResponse.get());
        } else {
            ClientErrorResponse errorResponse =
                    new ClientErrorResponse(500, "Error", "Failed to retrieve chat response.");
            responseEntity = new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        pushResponseToSse(conversationId, questionId, responseEntity);
    }


    private void resultToClient(String conversationId, Long questionId, Optional<AIGenerativeResponse> optionalResponse) {
        ResponseEntity<?> responseEntity;
        if (optionalResponse.isPresent()) {
            responseEntity = ResponseEntity.ok(optionalResponse.get());
        } else {
            ClientErrorResponse errorResponse =
                    new ClientErrorResponse(500, "Error", "Failed to retrieve chat response.");
            responseEntity = new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        pushResponseToSse(conversationId, questionId, responseEntity);
    }

    private void sendExceptionToClient(Throwable ex, HttpStatus status, String conversationId, Long questionId) {
        ClientErrorResponse errorResponse = new ClientErrorResponse(status.value(), "Error", ex.getMessage());
        ResponseEntity<ClientErrorResponse> responseEntity = new ResponseEntity<>(errorResponse, status);
        pushResponseToSse(conversationId, questionId, responseEntity);
    }

    private void pushResponseToSse(String conversationId, Long questionId, ResponseEntity<?> responseEntity) {
        String streamId = getStreamId(conversationId, questionId);
        SseEmitter emitter = getEmitter(streamId);
        if (emitter != null) {
            try {
                Assert.notNull(responseEntity.getBody(), "Report data body is null");
                log.info("Sending event to sse conversationId={}, questionId={}", conversationId, questionId);
                emitter.send(SseEmitter.event().data(responseEntity));
                emitter.complete();
            } catch (IOException e) {
                log.error("Failed to send SSE event for streamId={}", streamId, e);
                emitter.completeWithError(e);
            } finally {
                cleanupEmitter(streamId);
            }
        }
    }

    private SseEmitter getEmitter(String streamId) {
        SseEmitter emitter = emitters.get(streamId);
        if (emitter == null) {
            log.warn("No SSE emitter found for streamId={}. Client may have disconnected or never subscribed.", streamId);
        }
        return emitter;
    }

    @Override
    public void destroy() {
        log.info("Cancelling {} SSE heartbeat task(s)...", heartbeatTasks.size());
        heartbeatTasks.forEach((streamId, task) -> task.cancel(false));
        heartbeatTasks.clear();
        log.info("Shutting down SSE thread pool executor...");
        sseTaskExecutor.shutdown();
        try {
            boolean terminated = sseTaskExecutor.getThreadPoolExecutor().awaitTermination(30, TimeUnit.SECONDS);
            if (!terminated) {
                log.warn("SSE thread pool did not terminate within the timeout, forcing shutdown.");
                sseTaskExecutor.getThreadPoolExecutor().shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted during SSE thread pool shutdown", e);
            sseTaskExecutor.getThreadPoolExecutor().shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

