# API — Webhook Delivery Hub

Base: http://localhost:8080. Content-Type: application/json.

| Método | Ruta | Contrato |
| --- | --- | --- |
| POST | `/api/events` | 202 con Location |
| GET | `/api/deliveries` | Últimas 50 entregas |
| GET | `/api/deliveries/{id}` | Estado e historial |
| POST | `/api/deliveries/{id}/replay` | 200 si FAILED; 409 si activa |

## Request
```json
{
  "type": "payment.approved",
  "message": "fictional-payment"
}
```

## Response (campos relevantes)
```json
{
  "id": "8c7408b5-ff5e-42d1-b078-6172588da913",
  "status": "PENDING",
  "attempts": 0
}
```

## Errores
400 indica validación o formato inválido; 404 indica recurso inexistente. Los estados específicos se detallan en la tabla. ProblemDetail se usa para errores de negocio y validación donde aplica; autenticación puede devolver cuerpo vacío y WWW-Authenticate. Los clientes deben usar códigos, no parsear mensajes internos.

type: [a-z.], hasta 40 caracteres; message: 1–512. Reintentos para 429, 5xx y conexión; otros 4xx son definitivos. Cinco intentos por ciclo.

