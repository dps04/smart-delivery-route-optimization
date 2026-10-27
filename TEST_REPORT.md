# Test report

**Test environment:** OpenJDK 21, local Linux container, sample CSV files shipped with the project.  
**Test date:** 2026-10-27.  
**Scope:** compile, core regression assertions and HTTP API smoke test. These are actual local checks; they are not institutional acceptance tests.

| Test ID | Test | Expected result | Observed result | Status |
|---|---|---|---|---|
| T01 | Compile Java source with `javac` | Compilation succeeds | Compilation succeeded | PASS |
| T02 | Load sample order file | 6 orders read | 6 orders read | PASS |
| T03 | Allocate sample orders | 6 of 6 assigned | 6 of 6 assigned | PASS |
| T04 | Check vehicle capacity | Assigned load does not exceed capacity | V1 7/7; V2 7/8 | PASS |
| T05 | Baseline route KPI | 48 km for sample input | 48 km | PASS |
| T06 | Optimized route KPI | 43 km for sample input | 43 km | PASS |
| T07 | KPI formula | 10.42% reduction | 10.42% | PASS |
| T08 | Validation warnings for clean sample | No warnings | No warnings | PASS |
| T09 | HTTP API smoke test | `/api/optimize` returns JSON | HTTP response returned expected counts/routes/KPIs | PASS |

## Screenshot status
The dashboard HTML and API are available locally, but an actual browser screenshot could not be captured in this environment because browser navigation was blocked by the runtime. After launching the app on your computer, click **Run Optimization** and capture the dashboard with the KPI cards and route table visible. Do not mark screenshot evidence as attached until you save that real screenshot.

## Result interpretation
For the synthetic sample only, the baseline route distance is 48 km and nearest-neighbour route distance is 43 km. Reduction is `(48 - 43) / 48 × 100 = 10.42%`. The six orders are allocated with no capacity violation in this run.

## Tests not claimed
No cloud deployment, live traffic integration, load/concurrency test, security audit or test against the institution's original datasets has been performed. Invalid-input scenarios are described in the workbook's test plan but were not all exercised in the current run.
