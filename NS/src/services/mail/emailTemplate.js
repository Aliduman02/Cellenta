const generateEmailTemplate = (parsed) => {
  const isLimitExceeded = parsed.notification_message === "LIMIT_EXCEEDED";

  const usagetype = (type, parsed) => {
    if (!type) return "";
    const lower = type.toLowerCase();
    if (lower === "data") return "GB";
    if (lower === "minutes") return "dakika";
    if (lower === "sms") return "SMS";
    console.log("type:", type);
    return type;
  };

  const total = (type, parsed) => {
    if (!type || !parsed) return "";

    const lower = type.toLowerCase();
    const map = {
      data: "total_data",
      sms: "total_sms",
      minutes: "total_minutes",
    };
    const key = map[lower];
    let value = parsed?.[key];
    if (lower === "data" && value) {
      const gb = (Number(value) / 1000).toFixed(1);
      return gb;
    }
    return value || "0";
  };

  const remaining = (type, parsed) => {
    if (!type || !parsed) return "";
    const lower = type.toLowerCase();
    const map = {
      data: "remaining_data",
      sms: "remaining_sms",
      minutes: "remaining_minutes",
    };
    const key = map[lower];
    let value = parsed?.[key];
    if (lower === "data" && value) {
      const rem_gb = (Number(value) / 1000).toFixed(1);
      return rem_gb;
    }
    return value || "0";
  };
  const date = new Date();
  const options = {
    timeZone: "Europe/Istanbul",
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  };

  const istanbulFormatted = new Intl.DateTimeFormat("tr-TR", options).format(
    date
  );
  const alertHTML = isLimitExceeded
    ? `<div class="alert exceeded">Paketinizin ${usagetype(
        parsed.usage_type
      )} haklarını tamamen tükettiniz.<br\> Kullanıma devam etmek istiyorsanız ek ${usagetype(
        parsed.usage_type
      )} paketi almanız gerekmektedir. ❗</div>`
    : `<div class="alert">Paketinizin ${usagetype(
        parsed.usage_type
      )} haklarını %${parsed.percentage} kullandınız. ⚠️</div>`;
  return `
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <style>
      body {
        font-family: Arial, sans-serif;
        font-size: 16px;
        background: #ffffff;
        padding: 0;
        margin: 0;
      }
      .container {
        background: #fff;
        margin: 30px auto;
        max-width: 900px;
        border-radius: 8px;

        box-shadow: 0 0 10px rgba(0, 0, 0, 0.05);
        border: 2px solid rgba(0, 0, 0, 0.1);
      }
      .header {
        text-align: center;
        padding: 20px;
        background: linear-gradient(
          41deg,
          rgba(22, 105, 143, 1) 0%,
          rgba(0, 199, 190, 1) 100%
        );

        border-bottom: 1px solid rgba(0, 0, 0, 0.2);
      }
      .header img {
        max-width: 400px;
      }
      .content {
        padding: 30px;
        color: #333;
      }
      .title {
        font-size: 20px;
        color: #362c94;
      }
      .alert {
        background: #fff3cd;
        border: 1px solid #ffeeba;
        color: #856404;
        padding: 15px;
        border-radius: 4px;
        margin-top: 20px;
        font-size: 20px;
      }
      .alert.exceeded {
        background: #f8d7da;
        border-color: #f5c6cb;
        color: #721c24;
      }
      .footer {
        text-align: center;
        color: #999;
        margin-top: 20px;
        padding: 10px;
        padding-top: 30px;
        border-top: 1px solid #b8b8b8;
      }
      .usage-card {
        background-color: #f9f7fc;
        padding: 20px;
        border-radius: 10px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
      }
      .usage-card h3 {
        font-size: 18px;
        margin-bottom: 16px;
        color: #362c94;
      }
    </style>
  </head>
  <body>
    <div class="container">
      <div class="header">
        <img src="cid:cellenta-logo" alt="Cellenta " width="500" />
      </div>

      <div class="content">
        <div class="title">Merhaba ${parsed.name} ${parsed.surname},</div>
        <p>
          <strong>${parsed.package_name}</strong> kullanıcısı olarak,
          <strong
            >${istanbulFormatted}</strong
          > tarihinde yaptığınız son kullanım doğrultusunda
        </p>
        ${alertHTML}
        <div class="usage-card">
          <h3>Kullanım bilgileriniz aşağıda belirtilmiştir:</h3>
          <div id="progressbar">
            <div></div>
          </div>
          <div class="usage-info">
            <ul>
              <li>
                <strong>Kalan Kullanım Hakkınız:</strong> ${remaining(
                  parsed.usage_type,
                  parsed
                )}  / ${total(parsed.usage_type, parsed)} ${usagetype(
    parsed.usage_type
  )}
              </li>
              <li>
                <strong>Paket Başlangıç Tarihi:</strong> ${new Date(
                  parsed.package_start_date
                ).toLocaleDateString("tr-TR")}
              </li>
              <li>
                <strong>Paket Son Kullanım Tarihi:</strong> ${new Date(
                  parsed.package_end_date
                ).toLocaleDateString("tr-TR")}
              </li>
            </ul>
          </div>
        </div>

        <div class="footer">
          <table width="100%" cellpadding="0" cellspacing="0" border="0">
            <tr>
              <td style="text-align: center">
                Bu e-posta ${istanbulFormatted} tarihinde
                oluşturulmuştur.
              </td>
            </tr>
            <tr>
              <td style="text-align: center">
                Sorularınız için
                <a href="" style="color: #4b0082; text-decoration: none"
                  >bizimle iletişime geçebilirsiniz</a
                >.
              </td>
            </tr>
            <tr>
              <td style="text-align: center">
                © 2025 Cellenta – Tüm hakları saklıdır.
              </td>
            </tr>
          </table>
        </div>
      </div>
    </div>
  </body>
</html>
`;
};

export default generateEmailTemplate;
