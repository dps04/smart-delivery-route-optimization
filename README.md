# Smart Delivery Route and Resource Optimization System

**Student:** Dhirender Pratap Singh  
**E No.:** O25MSD110099  
**Course:** M.Sc (Data Science) - V2  
**Project ID:** MSDS-MINOR-OPT-001

A small, dependency-free Java 21 web prototype for a delivery-routing minor project. It reads three CSV files, checks basic data constraints, allocates orders to compatible available vehicles without exceeding capacity, builds a baseline route and a nearest-neighbour route per vehicle, and displays route distances and a comparison dashboard.

> **Dataset disclaimer:** `data/*_Sample.csv` contains synthetic example data created for this project because the original institution/client datasets were not provided. Results below are illustrative and are not operational/business claims.

## Requirements
- JDK 21 or later (JDK 17 may work, but the project is tested with JDK 21)
- A modern browser
- No third-party Java libraries or build tool required

## Run locally (Windows PowerShell)
From the project root:

```powershell
New-Item -ItemType Directory -Force build\classes | Out-Null
javac -d build\classes src\main\java\com\logismart\*.java
java -cp build\classes -Ddata.dir=data -Dport=8080 com.logismart.App
```
Then open `http://localhost:8080` and select **Run Optimization**. If port 8080 is in use, replace `8080` with `8090` in the command.

## Run locally (macOS/Linux)
```bash
mkdir -p build/classes
javac -d build/classes src/main/java/com/logismart/*.java
java -cp build/classes -Ddata.dir=data -Dport=8080 com.logismart.App
```
Open `http://localhost:8080`.

## Run the regression checks
```bash
mkdir -p build/classes
javac -d build/classes src/main/java/com/logismart/*.java tests/SelfTest.java
java -ea -cp build/classes SelfTest
```
Expected output includes `PASS` and `Baseline=48 km; optimized=43 km; reduction=10.42%` for the included synthetic dataset.

## API
- `GET /` — dashboard HTML
- `GET /api/optimize` — JSON optimization result

## Algorithm and assumptions
1. Orders are sorted High, Medium, Low priority, then by order ID.
2. Each order is assigned to the first available matching vehicle with enough remaining capacity. This is a simple deterministic first-fit heuristic, not a globally optimal vehicle allocation solver.
3. The baseline route follows each assigned vehicle's order sequence in the CSV (plus a return to depot A).
4. The optimized route uses nearest-neighbour selection starting at depot A, visiting the closest unvisited assigned destination, then returning to A.
5. Distances are kilometres from the supplied sample matrix. The sample assumes symmetric distances and uses depot `A`.
6. Cost, real traffic, delivery time windows and real-world on-time performance are not modelled because the provided workbook did not include those inputs.

## Sample result
| Measure | Baseline | Optimized |
|---|---:|---:|
| Total route distance | 48 km | 43 km |
| Distance reduction | — | 10.42% |
| Orders allocated | 6/6 | 6/6 |
| Capacity violations | 0 | 0 |

Formula: `(baseline km - optimized km) / baseline km × 100`. With sample data: `(48 - 43) / 48 × 100 = 10.42%`.

## Project structure
```text
src/main/java/com/logismart/  Java HTTP server and optimization logic
 data/                         Synthetic CSV input files
 tests/SelfTest.java            Dependency-free regression checks
 docs/                          Architecture and test notes
 evidence/                       Folder reserved for genuine screenshots captured by the student
 Dockerfile                      Container build/run definition
```

## Limitations
- The nearest-neighbour heuristic does not guarantee a globally shortest route.
- Synthetic sample data is small and must not be presented as official operational data.
- No live traffic, time windows, persistence, authentication or production deployment is included.
- The included container definition is a deployment recipe; it does not prove that the project has been deployed to a cloud provider.
