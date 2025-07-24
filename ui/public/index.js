let table;
fetch("http://34.141.21.67:3000/api/sms-logs")
  .then((res) => res.json())
  .then((data) => {
    table = new Tabulator("#log-table", {
      data: data,
      layout: "fitColumns",
      history: true,
      pagination: "local",
      paginationSize: 20,
      downloadRowRange: "selected",
      paginationCounter: "rows",
      selectableRows: true,
      selectable: true,
      rowClick: (e, row) => row.toggleSelect(),
      columns: [
        { title: "Telefon", field: "msisdn", widthGrow: 0.7 },
        {
          title: "Mesaj",
          field: "message",
          widthGrow: 8,
        },
        {
          title: "Tip",
          field: "usage_type",
          headerFilter: "select",
          headerFilterParams: {
            values: { sms: "sms", minutes: "minutes", data: "data" },
          },
        },
        {
          title: "Durum",
          field: "notification_message",
          headerFilter: "select",
          headerFilterParams: {
            values: {
              WARNING: "WARNING",
              LIMIT_EXCEEDED: "LIMIT_EXCEEDED",
            },
          },
        },
        {
          title: "Zaman",
          field: "timestamp",
          formatter: (cell) => {
            const d = new Date(cell.getValue());
            return d.toLocaleString("tr-TR", {
              timeZone: "Europe/Istanbul",
            });
          },
        },
      ],
    });

    const getRowRange = () => {
      const selected = table.getSelectedRows();
      return selected.length > 0 ? "selected" : "visible";
    };

    document.getElementById("download-csv").addEventListener("click", () => {
      bom: true, table.download("csv", "data.csv", { rowRange: getRowRange() });
    });

    document.getElementById("download-json").addEventListener("click", () => {
      bom: true,
        table.download("json", "data.json", {
          rowRange: getRowRange(),
        });
    });

    document.getElementById("download-pdf").addEventListener("click", () => {
      bom: true,
        table.download("pdf", "data.pdf", {
          orientation: "portrait",
          title: "Cellenta SMS Logları",
          rowRange: getRowRange(),
        });
    });

    document.getElementById("download-html").addEventListener("click", () => {
      bom: true,
        table.download("html", "data.html", {
          style: true,
          rowRange: getRowRange(),
        });
    });

    document
      .getElementById("global-search")
      .addEventListener("keyup", function () {
        const query = this.value.toLowerCase();
        table.setFilter((row) =>
          Object.values(row).some((val) =>
            String(val).toLowerCase().includes(query)
          )
        );
      });

    table.on("rowSelectionChanged", function (data, rows) {
      document.getElementById("select-stats").innerHTML = data.length;
    });

    document.getElementById("select-row").addEventListener("click", () => {
      table.selectRow();
    });

    document.getElementById("deselect-row").addEventListener("click", () => {
      table.deselectRow();
    });
  })
  .catch((err) => console.error("Veri alınamadı:", err));
