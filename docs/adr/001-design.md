# ADR 001 — DELIVERY

Estado: aceptada · 2026-10-05

## Contexto
Una notificación no debe desaparecer cuando el receptor cae después de confirmar una operación. El evento y su entrega pendiente se guardan en una sola transacción.

## Decisión
La llamada HTTP ocurre fuera de la transacción. Si el proceso muere después de entregar y antes de guardar el resultado, puede reenviar: la semántica es at-least-once. El receptor verifica HMAC y deduplica por eventId. La API no recibe URLs y el cliente no sigue redirecciones, reduciendo la superficie SSRF.

## Alternativas
Separar más microservicios o incorporar un broker agregaría despliegue y operación fuera del objetivo. Concentrar todo en el controlador dificultaría probar fallos y razonar sobre el contrato. Se elige una aplicación pequeña con API, casos de uso y adaptadores diferenciados.

## Consecuencias
Worker para una instancia. El receptor deduplica en memoria durante una hora (máximo 10000 IDs); producción requiere deduplicación durable. Replay reinicia el historial del ciclo, no conserva una auditoría ilimitada. Una entrega SENDING abandonada se recupera al expirar su lease de 30 segundos.

## Validación
HTTP real, firma HMAC, reintento tras 500, rechazo de replay activo y límites de backoff.
