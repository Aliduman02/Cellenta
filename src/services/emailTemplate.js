const generateEmailTemplate = ({
  customer,
  packageInfo,
  usageAlert,
  timestamp,
}) => {
  const isLimitExceeded = usageAlert.notificationType === "LIMIT_EXCEEDED";

  const alertHTML = isLimitExceeded
    ? `<div class="alert exceeded">${usageAlert.usageType} Paketinizi tamamen tükettiniz.<br\> Kullanıma devam etmek istiyorsanız ek ${usageAlert.usageType} paketi almanız gerekmektedir. ❗</div>`
    : `<div class="alert">Paketinizin %${usageAlert.usagePercentage} kullanım sınırına ulaştınız. ⚠️</div>`;

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
        padding: 20px;
        padding-top: 50px;
        border-top: 1px solid #b8b8b8;
      }
    </style>
  </head>
  <body>
    <div class="container">
      <div class="header">
        <img src="cid:cellenta-logo" alt="Cellenta "width="500" />
      </div>

      <div class="content">
        <div class="title">Merhaba ${customer.name} ${customer.surname},</div>
        <p>
          <strong>${
            packageInfo.packageName
          }</strong> kullanıcısı olarak,  <strong>${new Date(
    timestamp
  ).toLocaleString("tr-TR")}</strong> tarihinde
          yaptığınız son <strong>${usageAlert.usageType}</strong> kullanımı
          doğrultusunda <br />${alertHTML} <br /><br />Kullanım
          bilgileriniz aşağıda belirtilmiştir:
        </p>

        <ul>
          <li><strong>Kullanım Türü:</strong> ${usageAlert.usageType}</li>
          <li><strong>Paket Limiti:</strong> ${usageAlert.packageLimit} MB</li>
          <li><strong>Kullanılan:</strong> ${usageAlert.usedAmount} MB</li>
          <li><strong>Kalan:</strong> ${usageAlert.remainingAmount} MB</li>
          <li>
            <strong>Paket Başlangıç Tarihi:</strong> ${new Date(
              packageInfo.startDate
            ).toLocaleDateString("tr-TR")}
          </li>
          <li>
            <strong>Paket Bitiş Tarihi:</strong> ${new Date(
              packageInfo.endDate
            ).toLocaleDateString("tr-TR")}
          </li>
        </ul>

        <div class="footer">
          Bu e-posta ${new Date().toLocaleString("tr-TR")} tarihinde
          oluşturulmuştur.<br/>
          Sorularınız için
          <a href="" target="_blank">bizimle iletişime geçebilirsiniz</a>.<br/>
          © 2025 Cellenta – Tüm hakları saklıdır.
        </div>
      </div>
    </div>
  </body>
</html>
`;
};

export default generateEmailTemplate;
