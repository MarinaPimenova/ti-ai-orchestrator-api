package com.wk.ti.agents.registry;

import com.wk.ti.agents.Agents;
import com.wk.ti.agents.nodes.AgentNode;
import com.wk.ti.agents.tool.document.DocumentTool;
import com.wk.ti.agents.tool.hello.HelloTool;

import com.wk.ti.agents.tool.question.sql.QuestionSqlTool;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class AgentRegistry {

    private final List<AgentNode> agents;

    private final ChatClient chatClient;

    private final HelloTool helloTool;
    private final DocumentTool documentTool;
    private final QuestionSqlTool questionSqlTool;

    public AgentRegistry(
            ChatClient chatClient,
            HelloTool helloTool,
            DocumentTool documentTool,
            QuestionSqlTool questionSqlTool) {
        this.chatClient = chatClient;
        this.helloTool = helloTool;
        this.documentTool = documentTool;
        this.questionSqlTool = questionSqlTool;
        this.agents = new ArrayList<>();
    }

    @PostConstruct
    public void initialize() {
        AgentNode routingAgent = new AgentNode(Agents.ROUTING_AGENT.name(),
                """
                        You are a strict intent-based router.
                        Your job: analyze the user's INTENT (what they are trying to accomplish) and route to the correct agent(s).
                        STRICT RULE:
                        Return ONLY valid JSON. No markdown. No explanation outside JSON.
                        
                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        AVAILABLE AGENTS
                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        
                        HELLO_AGENT
                          Purpose: Handle greetings, off-topic questions, and anything explicitly unrelated to another agents operations.
                        
                        DOCUMENT_AGENT
                          Purpose: Retrieve and process the CONTENT of documents.
                        
                        QUESTION_SQL_AGENT
                          Purpose: answer questions about the questions in the knowledge database.
                        
                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        ROUTING RULES (evaluate in order of priority)
                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        1. HELLO_AGENT — if the user's intent is ANY of:
                           - Greeting, small talk, thanks, or farewell WITH NO work-related subject.
                           - General knowledge EXPLICITLY unrelated to the others agents.
                           - Asking what the assistant can do, telling jokes, or any explicitly off-topic request.
                        2. DOCUMENT_AGENT - if the user's intent is to:
                           - Search specific information related to documents.
                        3. QUESTION_SQL_AGENT - if the user's intent is to:
                           - Search any information about questions that are stored in the TI Knowledge Platform (in the knowledge database).
                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        OUTPUT FORMAT (strict JSON only, no text outside JSON)
                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        
                        Respond with a JSON object containing exactly three fields:
                        
                        {
                          "reasoning": "Brief explanation of the detected intent and why you chose these agents. 1-2 sentences max.",
                          "confidence": 0.0 to 1.0,
                          "agents": {
                            "AGENT_KEY": "full user question"
                          }
                        }
                        
                        Field definitions:
                        - "reasoning": A short explanation of the detected intent and why the selected agents are appropriate.
                        - "confidence": A float between 0.0 and 1.0:
                          - 0.9 – 1.0 → Very clear intent, unambiguous routing
                          - 0.7 – 0.89 → Likely correct, minor ambiguity
                          - 0.5 – 0.69 → Uncertain, could reasonably go to another agent
                          - Below 0.5 → Highly ambiguous, consider asking the user to clarify
                        - "agents": Object where each key is an agent type and each value is the full user question string.
                        
                        CRITICAL: The value for each agent key inside "agents" must be the EXACT original user question.
                        Do NOT rephrase, rewrite, summarize, correct grammar, or modify the user's question in any way.
                        Copy the user's input text verbatim as-is.
                        
                        If a question is mapped to HELLO_AGENT:
                        {
                          "reasoning": "explanation",
                          "confidence": 0.0,
                          "agents": {
                            "HELLO_AGENT": "user question text"
                          }
                        }                        
                        
                        If a question is mapped to DOCUMENT_AGENT:
                        {
                          "reasoning": "explanation",
                          "confidence": 0.0,
                          "agents": {
                            "DOCUMENT_AGENT": "user question text"
                          }
                        }
                        
                        If a question is mapped to QUESTION_SQL_AGENT:
                        {
                          "reasoning": "explanation",
                          "confidence": 0.0,
                          "agents": {
                            "QUESTION_SQL_AGENT": "user question text"
                          }
                        }
                        
                        If a question is mapped to DOCUMENT_AGENT + QUESTION_SQL_AGENT:
                        {
                          "reasoning": "explanation",
                          "confidence": 0.0,
                          "agents": {
                            "DOCUMENT_AGENT": "user question text",
                            "QUESTION_SQL_AGENT": "user question text"
                          }
                        }
                                                                       
                        """,
                null,
                chatClient
        );

        AgentNode helloAgent = new AgentNode(
                Agents.HELLO_AGENT.name(),
                """
                        You are a HELLO Agent.
                        
                        Your job is to:
                        - Respond politely to greetings
                        - Explain what the system can do
                        - Help users understand available features
                        
                        Always call the greeting tool to generate the response.
                        Do not answer from your own knowledge.
                        
                        Keep responses friendly and concise.
                        """,
                ToolCallbacks.from(helloTool),
                chatClient
        );

        AgentNode questionSqlAgent = new AgentNode(Agents.QUESTION_SQL_AGENT.name(),
                """
                        You are a QUESTION SQL AGENT.
                        
                        Your task:
                        - Retrieve and analyze information from the TI Knowledge platform.
                        
                        Rules:
                        - Always use question sql tool.
                        - Do NOT answer without retrieving data.
                        - Do NOT make up content.
                        
                        Output:
                        - Return a list of questions (question + short answer + resources).
                        """,
                ToolCallbacks.from(questionSqlTool),
                chatClient
        );

        AgentNode documentAgent = new AgentNode(Agents.DOCUMENT_AGENT.name(),
                """
                        You are a DOCUMENT Agent.
                        
                        Your task:
                        - Retrieve and analyze information from documents.
                        
                        Rules:
                        - Always use the document tool.
                        - Do NOT answer without retrieved data.
                        - Base your answer ONLY on document content.
                        
                        Output:
                        - Provide a clear and concise answer from documents.
                        """,
                ToolCallbacks.from(documentTool),
                chatClient
        );
        agents.add(routingAgent);
        agents.add(helloAgent);

        agents.add(documentAgent);
        agents.add(questionSqlAgent);
    }

    public AgentNode get(String agentName) {
        Optional<AgentNode> agent = agents.stream()
                .filter(a -> a.getAgentName().equals(agentName))
                .findFirst();
        return agent.orElseThrow(() -> new RuntimeException("Agent not found: " + agentName));
    }

}
