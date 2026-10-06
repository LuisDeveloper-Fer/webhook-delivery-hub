# Validación

Trabajo realizado: 2026-10-05–2026-10-06 (America/Lima).

- Backend: Java 21.0.6, Maven 3.9.9. Pruebas locales ejecutadas: 3, sin fallos; confirmadas también en GitHub Actions.
- Angular: build de producción con comprobación estricta de TypeScript/templates.
- Docker Compose: configuración validada con docker compose config --quiet.
- Docker Engine local no disponible. **Docker Compose sí se ejecutó y pasó en GitHub Actions**, con API, Angular y servicios auxiliares reales.
- Código verificado: `c2b9e70`. [Ejecución exitosa: backend + frontend + stack](https://github.com/LuisDeveloper-Fer/webhook-delivery-hub/actions/runs/37416804322).
- El commit posterior de capturas/documentación no modifica el código validado.
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
