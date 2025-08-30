// Reconciliation Plant Interactive System Map - JavaScript

class ReconciliationPlant {
    constructor() {
        this.components = new Map();
        this.connections = [];
        this.isFlowActive = false;
        this.metrics = {
            totalProcessed: 0,
            errorCount: 0,
            dedupCount: 0,
            throughput: 1247,
            matchRate: 94.2,
            errorRate: 0.3,
        };

        this.init();
    }

    init() {
        this.setupEventListeners();
        this.setupComponents();
        this.setupConnections();
        this.startMetricsSimulation();
        this.setupDialControls();
    }

    setupEventListeners() {
        // Legend toggle
        document.getElementById("legendBtn").addEventListener("click", () => {
            this.toggleLegend();
        });

        // Flow toggle
        document.getElementById("flowBtn").addEventListener("click", () => {
            this.toggleDataFlow();
        });

        // Close buttons
        document.getElementById("closeLegend").addEventListener("click", () => {
            this.closeLegend();
        });

        document.getElementById("closeDetail").addEventListener("click", () => {
            this.closeDetail();
        });

        // Component clicks
        document.querySelectorAll(".component").forEach((component) => {
            component.addEventListener("click", (e) => {
                this.showComponentDetail(e.currentTarget);
            });
        });

        // Keyboard shortcuts
        document.addEventListener("keydown", (e) => {
            if (e.key === "Escape") {
                this.closeLegend();
                this.closeDetail();
            }
            if (e.key === "l" || e.key === "L") {
                this.toggleLegend();
            }
            if (e.key === "f" || e.key === "F") {
                this.toggleDataFlow();
            }
        });
    }

    setupComponents() {
        // Define component details
        this.components.set("bank-dock", {
            name: "Blue Fleet Dock",
            technical: "bank-txn-topic",
            type: "Kafka Topic",
            description: "Raw bank/switch events (ISO 8583 JSON)",
            details: {
                "Data Format": "ISO 8583 JSON",
                Source: "Bank Switch Systems",
                Frequency: "Real-time stream",
                Partitions: "12 partitions",
                Retention: "7 days",
                Compression: "snappy",
            },
            connections: ["intake-bay"],
        });

        this.components.set("scheme-dock", {
            name: "Red Fleet Dock",
            technical: "scheme-txn-topic",
            type: "Kafka Topic",
            description: "Raw scheme/clearing lines (CSV→JSON)",
            details: {
                "Data Format": "CSV converted to JSON",
                Source: "Scheme/Clearing Files",
                Frequency: "Batch uploads",
                Partitions: "8 partitions",
                Retention: "7 days",
                Compression: "gzip",
            },
            connections: ["intake-bay"],
        });

        this.components.set("intake-bay", {
            name: "Stateless Intake & Sanitising Bay",
            technical: "Ingestion Service (ingestion-service JAR)",
            type: "Microservice",
            description: "Stateless ingestion with in-memory deduplication",
            details: {
                Function: "Parse, validate, dedupe, emit clean DTOs",
                Storage: "RAM-only dedup set (no persistence)",
                Validation: "JSON schema validation",
                Deduplication: "txnId + sourceType in-memory set",
                "Error Handling": "Reject chute + counters",
                Output: "TxnRecordDTO to green conveyor",
            },
            connections: ["green-conveyor"],
        });

        this.components.set("green-conveyor", {
            name: "Green Conveyor",
            technical: "ingested-txn-topic",
            type: "Kafka Topic",
            description: "Clean TxnRecordDTO JSON messages",
            details: {
                Payload: "TxnRecordDTO JSON",
                "Key Strategy": "txnId|sourceType",
                Partitions: "16 partitions",
                Retention: "3 days",
                Compression: "lz4",
                Consumer: "Match Engine Service",
            },
            connections: ["matcher-sorter"],
        });

        this.components.set("matcher-sorter", {
            name: "Twin-Belt Matcher & Sorter",
            technical: "Match Engine Service (match-engine JAR)",
            type: "Microservice",
            description: "Core reconciliation logic with exact/fuzzy matching",
            details: {
                "Write-Through": "Immediate Cassandra writes",
                "Batch Processing": "Groups by batchId from Scheduler",
                "Exact Matching": "All key fields identical",
                "Fuzzy Matching": "Configurable tolerances",
                "Output Chutes": "MATCHED, FUZZY, UNMATCHED, EXCEPTION",
                Storage: "Cassandra ledgers + match results",
            },
            connections: ["cassandra-shelves", "exception-desks"],
        });

        this.components.set("cassandra-shelves", {
            name: "Cassandra Shelves",
            technical: "Fast-write Operational Store",
            type: "Database",
            description: "High-performance operational data store",
            details: {
                Tables: "switch_ledger, scheme_ledger, match_result",
                Consistency: "Eventual consistency",
                Replication: "3 replicas",
                Partitioning: "By txn_id (switch/scheme), batch_id (results)",
                Indexing: "Primary key only (no secondary indexes)",
                Compaction: "Size-tiered compaction",
            },
            connections: ["report-office"],
        });

        this.components.set("exception-desks", {
            name: "Exception Desks",
            technical: "PostgreSQL ACID Store",
            type: "Database",
            description: "ACID-compliant store for manual review",
            details: {
                Tables: "exception_ticket, reconciliation_batch_control, tolerance_rule",
                ACID: "Full ACID compliance",
                Indexing: "B-tree indexes on foreign keys",
                Constraints: "Foreign key constraints",
                "Audit Trail": "All changes logged",
                Backup: "Point-in-time recovery",
            },
            connections: [],
        });

        this.components.set("foreman-clock", {
            name: "Foreman Clock",
            technical: "Scheduler Service (scheduler JAR)",
            type: "Microservice",
            description: "Orchestrates batch reconciliation jobs",
            details: {
                Schedule: "06:00 daily trigger",
                API: "POST /api/batches/start",
                "Batch Control": "Creates batch execution record",
                "Async Execution": "Triggers Match Engine batch",
                "Retry Logic": "3 retries with exponential backoff",
                Monitoring: "Job status tracking",
            },
            connections: ["matcher-sorter", "exception-desks"],
        });

        this.components.set("report-office", {
            name: "Print & Dispatch Office",
            technical: "Report Service (report-service JAR)",
            type: "Microservice",
            description: "Generates and distributes reconciliation reports",
            details: {
                Trigger: "Daily bell or API call",
                "Data Source": "Cassandra batch statistics",
                Formats: "CSV and JSON reports",
                Storage: "MinIO S3-compatible bucket",
                Distribution: "REST API for auditors",
                Archive: "Historical report retention",
            },
            connections: [],
        });

        this.components.set("glass-wall-noc", {
            name: "Glass-Wall Control Room",
            technical: "Micrometer → Prometheus → Grafana",
            type: "Monitoring Stack",
            description: "Real-time system monitoring and alerting",
            details: {
                Metrics: "/metrics endpoint on all JVMs",
                Collection: "Prometheus scraping",
                Visualization: "Grafana dashboards",
                Alerting: "Prometheus AlertManager",
                Retention: "30 days detailed, 1 year aggregated",
                SLAs: "Match rate >95%, Error rate <1%",
            },
            connections: [],
        });
    }

    setupConnections() {
        this.connections = [
            { from: "bank-dock", to: "intake-bay", type: "kafka" },
            { from: "scheme-dock", to: "intake-bay", type: "kafka" },
            { from: "intake-bay", to: "green-conveyor", type: "kafka" },
            { from: "green-conveyor", to: "matcher-sorter", type: "kafka" },
            {
                from: "matcher-sorter",
                to: "cassandra-shelves",
                type: "database",
            },
            { from: "matcher-sorter", to: "exception-desks", type: "database" },
            { from: "foreman-clock", to: "matcher-sorter", type: "api" },
            { from: "foreman-clock", to: "exception-desks", type: "database" },
            {
                from: "cassandra-shelves",
                to: "report-office",
                type: "database",
            },
        ];
    }

    toggleLegend() {
        const panel = document.getElementById("legend-panel");
        panel.classList.toggle("active");
    }

    closeLegend() {
        const panel = document.getElementById("legend-panel");
        panel.classList.remove("active");
    }

    toggleDataFlow() {
        this.isFlowActive = !this.isFlowActive;
        const flowBtn = document.getElementById("flowBtn");

        if (this.isFlowActive) {
            this.showDataFlow();
            flowBtn.textContent = "Hide Data Flow";
            flowBtn.style.background =
                "linear-gradient(135deg, #ff6b6b, #ee5a52)";
        } else {
            this.hideDataFlow();
            flowBtn.textContent = "Show Data Flow";
            flowBtn.style.background =
                "linear-gradient(135deg, #00ff88, #00cc6a)";
        }
    }

    showDataFlow() {
        const svg = document.getElementById("flow-svg");
        svg.innerHTML = `
            <defs>
                <marker id="arrowhead" markerWidth="10" markerHeight="7"
                 refX="9" refY="3.5" orient="auto">
                    <polygon points="0 0, 10 3.5, 0 7" fill="#00ff88" />
                </marker>
            </defs>
        `;

        this.connections.forEach((conn, index) => {
            const fromEl = document.getElementById(conn.from);
            const toEl = document.getElementById(conn.to);

            if (fromEl && toEl) {
                const fromRect = fromEl.getBoundingClientRect();
                const toRect = toEl.getBoundingClientRect();
                const svgRect = svg.getBoundingClientRect();

                const x1 = fromRect.left + fromRect.width / 2 - svgRect.left;
                const y1 = fromRect.bottom - svgRect.top;
                const x2 = toRect.left + toRect.width / 2 - svgRect.left;
                const y2 = toRect.top - svgRect.top;

                const path = document.createElementNS(
                    "http://www.w3.org/2000/svg",
                    "path"
                );
                const d = `M ${x1} ${y1} Q ${x1} ${y1 + 50} ${x2} ${y2}`;

                path.setAttribute("d", d);
                path.setAttribute("class", "flow-line");
                path.setAttribute("stroke", this.getConnectionColor(conn.type));
                path.setAttribute("stroke-width", "3");
                path.setAttribute("fill", "none");
                path.setAttribute("marker-end", "url(#arrowhead)");

                svg.appendChild(path);

                // Animate appearance
                setTimeout(() => {
                    path.classList.add("active");
                }, index * 200);
            }
        });
    }

    hideDataFlow() {
        const svg = document.getElementById("flow-svg");
        const paths = svg.querySelectorAll(".flow-line");

        paths.forEach((path, index) => {
            setTimeout(() => {
                path.classList.remove("active");
            }, index * 100);
        });

        setTimeout(() => {
            svg.innerHTML = `
                <defs>
                    <marker id="arrowhead" markerWidth="10" markerHeight="7"
                     refX="9" refY="3.5" orient="auto">
                        <polygon points="0 0, 10 3.5, 0 7" fill="#00ff88" />
                    </marker>
                </defs>
            `;
        }, 1000);
    }

    getConnectionColor(type) {
        switch (type) {
            case "kafka":
                return "#00ff88";
            case "api":
                return "#ffa500";
            case "database":
                return "#ff6347";
            default:
                return "#00bfff";
        }
    }

    showComponentDetail(element) {
        const componentId = element.id;
        const component = this.components.get(componentId);

        if (!component) return;

        const panel = document.getElementById("detail-panel");
        const title = document.getElementById("detail-title");
        const content = document.getElementById("detail-content");

        title.textContent = component.name;

        content.innerHTML = `
            <div class="detail-section">
                <h4>Technical Implementation</h4>
                <div class="tech-label">${component.technical}</div>
                <p><strong>Type:</strong> ${component.type}</p>
                <p><strong>Description:</strong> ${component.description}</p>
            </div>

            <div class="detail-section">
                <h4>Implementation Details</h4>
                <ul class="detail-list">
                    ${Object.entries(component.details)
                        .map(
                            ([key, value]) =>
                                `<li><strong>${key}:</strong> ${value}</li>`
                        )
                        .join("")}
                </ul>
            </div>

            ${
                component.connections.length > 0
                    ? `
                <div class="detail-section">
                    <h4>Connections</h4>
                    <ul class="detail-list">
                        ${component.connections
                            .map((conn) => {
                                const connComponent = this.components.get(conn);
                                return `<li>${
                                    connComponent ? connComponent.name : conn
                                } (${
                                    connComponent
                                        ? connComponent.technical
                                        : conn
                                })</li>`;
                            })
                            .join("")}
                    </ul>
                </div>
            `
                    : ""
            }

            ${this.getComponentSpecificInfo(componentId)}
        `;

        panel.classList.add("active");

        // Add pulse effect to the clicked component
        element.classList.add("pulse");
        setTimeout(() => {
            element.classList.remove("pulse");
        }, 2000);
    }

    closeDetail() {
        const panel = document.getElementById("detail-panel");
        panel.classList.remove("active");
    }

    getComponentSpecificInfo(componentId) {
        switch (componentId) {
            case "intake-bay":
                return `
                    <div class="detail-section">
                        <h4>Current Metrics</h4>
                        <ul class="detail-list">
                            <li>Total Processed: ${this.metrics.totalProcessed.toLocaleString()}</li>
                            <li>Error Count: ${this.metrics.errorCount}</li>
                            <li>Dedup Count: ${this.metrics.dedupCount}</li>
                        </ul>
                    </div>
                `;

            case "matcher-sorter":
                return `
                    <div class="detail-section">
                        <h4>Matching Algorithm Tolerances</h4>
                        <ul class="detail-list">
                            <li>Amount Tolerance: ±${
                                document.getElementById("amount-value")
                                    .textContent
                            }</li>
                            <li>Time Tolerance: ${
                                document.getElementById("time-value")
                                    .textContent
                            }</li>
                            <li>Merchant Similarity: ${
                                document.getElementById("merchant-value")
                                    .textContent
                            }</li>
                        </ul>
                    </div>
                `;

            case "glass-wall-noc":
                return `
                    <div class="detail-section">
                        <h4>Live Metrics</h4>
                        <ul class="detail-list">
                            <li>Throughput: ${this.metrics.throughput}/min</li>
                            <li>Match Rate: ${this.metrics.matchRate}%</li>
                            <li>Error Rate: ${this.metrics.errorRate}%</li>
                        </ul>
                    </div>
                `;

            default:
                return "";
        }
    }

    setupDialControls() {
        const amountTolerance = document.getElementById("amount-tolerance");
        const timeTolerance = document.getElementById("time-tolerance");
        const merchantSimilarity = document.getElementById(
            "merchant-similarity"
        );

        const amountValue = document.getElementById("amount-value");
        const timeValue = document.getElementById("time-value");
        const merchantValue = document.getElementById("merchant-value");

        amountTolerance.addEventListener("input", (e) => {
            const value = parseFloat(e.target.value);
            amountValue.textContent = (value / 100).toFixed(2);
        });

        timeTolerance.addEventListener("input", (e) => {
            timeValue.textContent = `${e.target.value}h`;
        });

        merchantSimilarity.addEventListener("input", (e) => {
            const value = parseInt(e.target.value);
            merchantValue.textContent = (value / 100).toFixed(1);
        });
    }

    startMetricsSimulation() {
        setInterval(() => {
            // Simulate metrics updates
            this.metrics.totalProcessed += Math.floor(Math.random() * 50) + 20;
            this.metrics.errorCount += Math.random() < 0.1 ? 1 : 0;
            this.metrics.dedupCount += Math.random() < 0.05 ? 1 : 0;

            // Update throughput with some variance
            this.metrics.throughput =
                1247 + Math.floor(Math.random() * 200) - 100;

            // Update match rate with small variance
            this.metrics.matchRate = 94.2 + Math.random() * 2 - 1;

            // Update error rate with small variance
            this.metrics.errorRate = 0.3 + Math.random() * 0.4 - 0.2;

            // Update DOM elements
            this.updateMetricsDisplay();
        }, 2000);
    }

    updateMetricsDisplay() {
        const elements = {
            "total-processed": this.metrics.totalProcessed.toLocaleString(),
            "error-count": this.metrics.errorCount,
            "dedup-count": this.metrics.dedupCount,
            throughput: this.metrics.throughput.toLocaleString() + "/min",
            "match-rate": this.metrics.matchRate.toFixed(1) + "%",
            "error-rate": this.metrics.errorRate.toFixed(1) + "%",
        };

        Object.entries(elements).forEach(([id, value]) => {
            const element = document.getElementById(id);
            if (element) {
                element.textContent = value;
            }
        });
    }

    // Utility method to highlight component relationships
    highlightRelationships(componentId) {
        const component = this.components.get(componentId);
        if (!component) return;

        // Reset all highlights
        document.querySelectorAll(".component").forEach((el) => {
            el.classList.remove("glow");
        });

        // Highlight current component
        document.getElementById(componentId).classList.add("glow");

        // Highlight connected components
        component.connections.forEach((connId) => {
            const connElement = document.getElementById(connId);
            if (connElement) {
                connElement.classList.add("glow");
            }
        });

        // Remove highlights after 3 seconds
        setTimeout(() => {
            document.querySelectorAll(".component").forEach((el) => {
                el.classList.remove("glow");
            });
        }, 3000);
    }
}

// Initialize the application when DOM is loaded
document.addEventListener("DOMContentLoaded", () => {
    const plant = new ReconciliationPlant();

    // Make plant instance available globally for debugging
    window.reconciliationPlant = plant;

    console.log("Reconciliation Plant Interactive System Map initialized");
    console.log(
        "Keyboard shortcuts: L = Toggle Legend, F = Toggle Flow, ESC = Close panels"
    );
});
