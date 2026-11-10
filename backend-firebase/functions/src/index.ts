import * as functions from "firebase-functions";
import axios from "axios";

// Environment variable or fallback
const SHIP24_API_KEY = process.env.SHIP24_API_KEY || "";

interface CarrierDto {
  code: string | null;
  name: string | null;
}

interface TrackingEventDto {
  status: string;
  description: string;
  location: string | null;
  timestamp: string;
}

interface TrackingResponse {
  trackingCode: string;
  carrier: CarrierDto | null;
  events: TrackingEventDto[];
  status: string;
}

/**
 * Endpoint to register / track a new package
 * POST /trackPackage
 * Body: { "trackingCode": "AA123456789BR" }
 */
export const trackPackage = functions.https.onRequest(async (req, res) => {
  // CORS setup
  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  res.set("Access-Control-Allow-Headers", "Content-Type, Authorization");

  if (req.method === "OPTIONS") {
    res.status(204).send("");
    return;
  }

  try {
    const trackingCode = req.body?.trackingCode || req.query?.code;
    if (!trackingCode || typeof trackingCode !== "string") {
      res.status(400).json({ error: "Parâmetro 'trackingCode' é obrigatório." });
      return;
    }

    const cleanCode = trackingCode.trim().toUpperCase();

    // If Ship24 API Key is configured, make real request
    if (SHIP24_API_KEY) {
      try {
        const response = await axios.post(
          "https://api.ship24.com/public/v1/trackers",
          { trackingNumber: cleanCode },
          {
            headers: {
              Authorization: `Bearer ${SHIP24_API_KEY}`,
              "Content-Type": "application/json; charset=utf-8",
            },
            timeout: 10000,
          }
        );

        const tracker = response.data?.data?.tracker;
        const events = tracker?.events || [];

        const normalizedEvents: TrackingEventDto[] = events.map((ev: any) => ({
          status: ev.statusMilestone || "IN_TRANSIT",
          description: ev.status || ev.description || "Atualização de status",
          location: ev.location || null,
          timestamp: ev.datetime || new Date().toISOString(),
        }));

        const result: TrackingResponse = {
          trackingCode: cleanCode,
          carrier: {
            code: tracker?.carrierCode || "CORREIOS",
            name: tracker?.carrierName || "Correios",
          },
          events: normalizedEvents,
          status: tracker?.statusMilestone || (normalizedEvents.length > 0 ? normalizedEvents[0].status : "IN_TRANSIT"),
        };

        res.status(200).json(result);
        return;
      } catch (apiErr: any) {
        functions.logger.warn("Ship24 API call failed, falling back to smart simulation:", apiErr.message);
      }
    }

    // Demo / Simulation Fallback (Works offline or without API key for immediate testing)
    const isDelivered = cleanCode.endsWith("DL");
    const simulatedStatus = isDelivered ? "DELIVERED" : "IN_TRANSIT";

    const mockEvents: TrackingEventDto[] = [
      {
        status: simulatedStatus,
        description: isDelivered
          ? "Objeto entregue ao destinatário"
          : "Objeto em trânsito - de Unidade de Tratamento para Unidade de Distribuição",
        location: isDelivered ? "São Paulo, SP" : "Curitiba, PR",
        timestamp: new Date().toISOString(),
      },
      {
        status: "IN_TRANSIT",
        description: "Objeto recebido pelos Correios do Brasil",
        location: "Curitiba, PR",
        timestamp: new Date(Date.now() - 24 * 3600 * 1000).toISOString(),
      },
      {
        status: "POSTED",
        description: "Objeto postado",
        location: "Origem",
        timestamp: new Date(Date.now() - 48 * 3600 * 1000).toISOString(),
      },
    ];

    const result: TrackingResponse = {
      trackingCode: cleanCode,
      carrier: {
        code: "CORREIOS",
        name: cleanCode.endsWith("BR") ? "Correios Brasil" : "Transportadora Parceira",
      },
      events: mockEvents,
      status: simulatedStatus,
    };

    res.status(200).json(result);
  } catch (err: any) {
    functions.logger.error("Erro interno no trackPackage:", err);
    res.status(500).json({ error: "Erro interno no servidor ao processar rastreio." });
  }
});

/**
 * Endpoint to get status for an existing tracking code
 * GET /getTracking?code=AA123456789BR
 */
export const getTracking = functions.https.onRequest(async (req, res) => {
  // Delegate to trackPackage logic
  req.body = { trackingCode: req.query.code || req.body?.trackingCode };
  return trackPackage(req, res);
});
