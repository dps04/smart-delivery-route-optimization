# Cloud-native architecture and design evidence

## Component flow

```text
Browser dashboard (HTML/CSS/JavaScript)
          |
          | HTTP GET / and /api/optimize
          v
Java HTTP server (com.sun.net.httpserver)
          |
          v
DeliveryOptimizer service
  |-- read Delivery Orders CSV
  |-- read Vehicle Master CSV
  |-- read Zone Distance Matrix CSV
  |-- validate IDs, loads, capacity and zone references
  |-- priority sorting + first-fit vehicle allocation
  |-- baseline route + nearest-neighbour route
  |-- KPI calculation and JSON response
          |
          v
CSV input files (mounted/copied data directory)
```

## Component responsibilities
- **Web dashboard:** starts a run, renders route/KPI tables and warnings.
- **HTTP/API layer:** serves the HTML interface and exposes the JSON endpoint.
- **Optimization service:** parses CSV, performs basic checks, allocates orders, computes routes and calculates KPIs.
- **Data directory:** holds the three sample CSV inputs. For production, use controlled persistent storage and validate upload permissions/file size.
- **Container runtime:** packages the JDK and source code in a repeatable environment. `Dockerfile` is a proposed local container deployment recipe.

## Cloud-native considerations
- Keep runtime settings such as port and data directory configurable using `-Dport` and `-Ddata.dir`.
- For a multi-user deployment, isolate per-run inputs/outputs and avoid shared mutable state.
- Add request limits, authentication/authorization, structured logs, health checks and persistent object storage before production use.
- Use a managed container platform and TLS termination if deploying publicly. No cloud deployment is claimed by this student package.
