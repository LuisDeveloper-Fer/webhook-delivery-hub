# Demostración en cinco minutos

## Problema — 30 segundos
Una notificación no debe desaparecer cuando el receptor cae después de confirmar una operación. El evento y su entrega pendiente se guardan en una sola transacción.

## Experimento — 2 minutos
Publica un evento normal. Después usa message=simulate-error o simulate-rate-limit. Inspecciona el historial hasta FAILED y ejecuta un replay manual.

## Decisión — 1 minuto
La llamada HTTP ocurre fuera de la transacción. Si el proceso muere después de entregar y antes de guardar el resultado, puede reenviar: la semántica es at-least-once. El receptor verifica HMAC y deduplica por eventId. La API no recibe URLs y el cliente no sigue redirecciones, reduciendo la superficie SSRF.

## Discusión
- ¿Qué operación es atómica?
- ¿Qué ocurre entre confirmar una escritura y enviar una respuesta?
- ¿Qué impide agotar recursos?
- ¿Qué cambia al ejecutar dos réplicas?
- ¿Qué mide el dashboard y qué no permite concluir?

## Límites que conviene explicar
Worker para una instancia. El receptor deduplica en memoria durante una hora (máximo 10000 IDs); producción requiere deduplicación durable. Replay reinicia el historial del ciclo, no conserva una auditoría ilimitada. Una entrega SENDING abandonada se recupera al expirar su lease de 30 segundos.
