
export default {
  async fetch(request, env) {
    const corsHeaders = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type"
    };

    if (request.method === "OPTIONS") {
      return new Response(null, {
        headers: corsHeaders
      });
    }

    const jsonResponse = (data, status = 200) =>
      new Response(JSON.stringify(data), {
        status,
        headers: {
          ...corsHeaders,
          "Content-Type": "application/json"
        }
      });

    if (request.method !== "POST") {
      return jsonResponse({ error: "Method not allowed" }, 405);
    }

    if (!env.GEMINI_API_KEY) {
      return jsonResponse({ error: "Backend API key is not configured" }, 500);
    }

    try {
      const { message } = await request.json();

      if (typeof message !== "string" || !message.trim()) {
        return jsonResponse({ error: "Message is required" }, 400);
      }

      if (message.length > 4000) {
        return jsonResponse({ error: "Message is too long" }, 400);
      }

      const model = env.GEMINI_MODEL || "gemini-2.5-flash";

      const response = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "x-goog-api-key": env.GEMINI_API_KEY
          },
          body: JSON.stringify({
            contents: [{
              parts: [{ text: message.trim() }]
            }]
          })
        }
      );

      const result = await response.json();

      if (!response.ok) {
        return jsonResponse({
          error: "Gemini request failed"
        }, 502);
      }

      const reply =
        result.candidates?.[0]?.content?.parts
          ?.map(part => part.text || "")
          .join("")
          .trim();

      if (!reply) {
        return jsonResponse({
          error: "Gemini returned an empty response"
        }, 502);
      }

      return jsonResponse({ reply });

    } catch (error) {
      return jsonResponse({
        error: "Unable to process your request"
      }, 500);
    }
  }
};
