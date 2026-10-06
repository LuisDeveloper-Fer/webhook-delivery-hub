# Validación

Fecha del trabajo: 2026-10-05 (America/Lima).

- Backend: Java 21.0.6, Maven 3.9.9. Pruebas locales ejecutadas: 3, sin fallos en la ejecución inicial.
- Angular: build de producción con comprobación estricta de TypeScript/templates.
- Docker Compose: configuración validada con docker compose config --quiet.
- Docker Engine local no disponible; la ejecución de contenedores se verifica en el job stack de GitHub Actions. Consulta su resultado para el commit actual; no se presupone éxito.
- Las pruebas Maven usan H2. El smoke test del stack utiliza PostgreSQL y comprueba el flujo HTTP principal. No equivale a una prueba de carga o a una certificación de compatibilidad completa.


## Reproducir

```bash
mvn clean package
cd frontend && npm ci && npm run build
cd ..
docker compose up -d --build
python scripts/smoke.py
docker compose down
```

Se usan datos ficticios. El smoke test crea registros nuevos; no debe ejecutarse contra entornos ajenos al laboratorio.
