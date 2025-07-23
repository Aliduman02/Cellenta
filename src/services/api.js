/**
 * API client for React application using fetch
 * @module ApiService
 */

// Base URL of the API
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://34.123.86.69';

// API timeout in milliseconds
const API_TIMEOUT = 10000;

// Debug mode
const DEBUG_MODE = process.env.NODE_ENV === 'development';

/**
 * @typedef {Object} AuthResponse
 * @property {string} token
 * @property {string} [access_token]
 * @property {string} [refreshToken]
 */

/**
 * @typedef {Object} BalanceResponse
 * @property {string} packageName
 * @property {number} price
 * @property {number} remainingMinutes
 * @property {number} remainingData
 * @property {number} remainingSms
 * @property {string} [period]
 * @property {string} [sdate]
 * @property {string} [edate]
 */

/**
 * @typedef {Object} PackageResponse
 * @property {number} package_id
 * @property {string} packageName
 * @property {number} price
 * @property {number} amountMinutes
 * @property {number} amountData
 * @property {number} amountSms
 * @property {string} period
 */

/**
 * @typedef {Object} BillResponse
 * @property {number} id
 * @property {string} date
 * @property {string} amount
 * @property {string} status
 * @property {string} [left]
 */

class ApiService {
  constructor() {
    this.baseURL = API_BASE_URL;
  }

  /**
   * Get stored auth token
   * @returns {string | null}
   */
  getAuthToken() {
    return localStorage.getItem('authToken');
  }

  /**
   * Save auth token
   * @param {string} token
   */
  setAuthToken(token) {
    localStorage.setItem('authToken', token);
  }

  /**
   * Remove auth token
   */
  removeAuthToken() {
    localStorage.removeItem('authToken');
  }

  /**
   * Generic API request
   * @param {string} path API endpoint path
   * @param {Object} options
   * @param {string} [options.method='GET']
   * @param {Object} [options.headers]
   * @param {Object} [options.body]
   * @returns {Promise<any>}
   */
  async request(path, { method = 'GET', headers = {}, body = null } = {}) {
    const url = `${this.baseURL}${path}`;
    // const token = this.getAuthToken(); // Artık kullanılmıyor

    const config = {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Device-Type': 'WEB',
        ...headers,
        // ...(token && { Authorization: `Bearer ${token}` }), // Kaldırıldı
      },
    };

    if (body) {
      config.body = JSON.stringify(body);
    }

    try {
      if (DEBUG_MODE) {
        console.log(`API Request: ${method} ${url}`, { headers: config.headers, body });
      }

      const response = await fetch(url, config);
      let data;

      try {
        data = await response.json();
      } catch (err) {
        data = null;
      }

      if (DEBUG_MODE) {
        console.log(`API Response: ${response.status}`, data);
      }

      if (!response.ok) {
        const message = data?.message || data?.error || response.statusText;
        throw new Error(`API Error [${response.status}]: ${message}`);
      }

      return data;
    } catch (error) {
      if (DEBUG_MODE) {
        console.error('API Error:', error.message);
      }
      throw error;
    }
  }

  /**
   * Login with phone and password
   * @param {string} phone msisdn
   * @param {string} password
   * @returns {Promise<AuthResponse>}
   */
  async login(phone, password) {
    try {
      const auth = await this.request('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Device-Type': 'WEB' },
        body: { 
          msisdn: phone, 
          password: password 
        },
      });

      // API'den token dönmüyor, sadece user bilgileri dönüyor
      // Bu durumda session-based auth kullanılıyor olabilir
      // Cookie'ler otomatik olarak gönderiliyor
      
      if (!auth.cust_id) {
        throw new Error('Login failed - no user ID received');
      }

      // Session-based auth için kullanıcı bilgilerini kaydet
      localStorage.setItem('userPhone', phone);
      localStorage.setItem('userId', auth.cust_id);
      localStorage.setItem('userName', auth.name);
      localStorage.setItem('userSurname', auth.surname);
      localStorage.setItem('userEmail', auth.email);
      localStorage.setItem('loginTimestamp', Date.now().toString());

      return auth;
    } catch (error) {
      throw error;
    }
  }

  /**
   * Register a new user
   * @param {Object} user
   * @param {string} user.phone
   * @param {string} user.password
   * @param {string} user.firstName
   * @param {string} user.lastName
   * @param {string} user.email
   * @returns {Promise<AuthResponse>}
   */
  async signup({ phone, password, firstName, lastName, email }) {
    try {
      const auth = await this.request('/api/v1/auth/register', {
        method: 'POST',
        headers: { 'Device-Type': 'WEB' },
        body: {
          msisdn: phone,
          password: password,
          name: firstName,
          surname: lastName,
          email: email,
        },
      });

      // API'den token dönmüyor, sadece user bilgileri dönüyor
      if (!auth.cust_id) {
        throw new Error('Registration failed - no user ID received');
      }

      // Session-based auth için kullanıcı bilgilerini kaydet
      localStorage.setItem('userPhone', phone);
      localStorage.setItem('userId', auth.cust_id);
      localStorage.setItem('userName', auth.name);
      localStorage.setItem('userSurname', auth.surname);
      localStorage.setItem('userEmail', auth.email);
      localStorage.setItem('loginTimestamp', Date.now().toString());

      return auth;
    } catch (error) {
      throw error;
    }
  }

  /**
   * Request password reset email
   * @param {string} email
   * @returns {Promise<Object>}
   */
  async forgotPassword(email) {
    return this.request('/api/v1/auth/forgot-password', {
      method: 'POST',
      headers: { 'Device-Type': 'WEB' },
      body: { email },
    });
  }

  /**
   * Verify reset code
   * @param {string} email
   * @param {string} code
   * @returns {Promise<Object>}
   */
  async verifyCode(email, code) {
    return this.request('/api/v1/auth/verify-code', {
      method: 'POST',
      headers: { 'Device-Type': 'WEB' },
      body: { email, code },
    });
  }

  /**
   * Reset password
   * @param {string} email
   * @param {string} code
   * @param {string} newPassword
   * @returns {Promise<Object>}
   */
  async resetPassword(email, code, newPassword) {
    return this.request('/api/v1/customers/change-password', {
      method: 'PATCH',
      headers: { 'Device-Type': 'WEB' },
      body: { email, verificationCode: code, password: newPassword },
    });
  }

  /**
   * Get user balance
   * @returns {Promise<BalanceResponse>}
   */
  async getBalance() {
    const msisdn = localStorage.getItem('userPhone');
    if (!msisdn) throw new Error('No user phone number');

    try {
      console.log('Fetching balance for msisdn:', msisdn);
      const response = await this.request('/api/v1/balance', {
        method: 'POST',
        body: { msisdn },
      });

      console.log('Balance API response:', response);
      return response;
    } catch (error) {
      console.error('Balance API failed:', error.message);
      throw new Error('Failed to fetch balance data');
    }
  }

  /**
   * Get user profile
   * @returns {Promise<Object>}
   */
  async getUserProfile() {
    let balance = {};
    try {
      balance = await this.getBalance();
    } catch (e) {
      // Balance API hatası varsa boş obje döndür
      balance = {};
    }
    const msisdn = localStorage.getItem('userPhone');
    const userId = localStorage.getItem('userId');
    const userName = localStorage.getItem('userName');
    const userSurname = localStorage.getItem('userSurname');
    const userEmail = localStorage.getItem('userEmail');

    return {
      cust_id: userId || '',
      msisdn: msisdn || '',
      name: userName || '',
      surname: userSurname || '',
      email: userEmail || '',
      packageName: balance.packageName || '',
      price: balance.price || 0,
      remainingMinutes: balance.remainingMinutes || 0,
      remainingData: balance.remainingData || 0,
      remainingSms: balance.remainingSms || 0,
    };
  }

  /**
   * Get dashboard usage data
   * @returns {Promise<Object>}
   */
  async getUsageData() {
    const balance = await this.getBalance();

    // API'den gelen verileri direkt kullan
    return {
      remainingMinutes: balance.remainingMinutes || 0,
      remainingData: balance.remainingData || 0,
      remainingSms: balance.remainingSms || 0,
      totalMinutes: balance.amountMinutes || 0,
      totalData: balance.amountData || 0,
      totalSms: balance.amountSms || 0,
    };
  }

  /**
   * Get active tariff details
   * @returns {Promise<Object>}
   */
  async getActiveTariff() {
    const balance = await this.getBalance();

    const tariff = {
      name: balance.packageName,
      price: balance.price,
      period: balance.period,
      startDate: balance.sdate,
      endDate: balance.edate,
    };

    console.log('Active tariff extracted from balance:', tariff);
    return tariff;
  }

  /**
   * Get bills list
   * @returns {Promise<BillResponse[]>}
   */
  async getBills() {
    const msisdn = localStorage.getItem('userPhone');
    if (!msisdn) throw new Error('No user phone number');

    // POST isteği ve msisdn body'de gönder
    const response = await this.request('/api/v1/customers/invoices', {
      method: 'POST',
      headers: { 'Device-Type': 'WEB' },
      body: { msisdn },
    });

    if (Array.isArray(response)) {
      return response;
    } else if (response && Array.isArray(response.data)) {
      return response.data;
    } else {
      throw new Error('Invalid bills response format');
    }
  }

  /**
   * Pay a bill
   * @param {number|string} billId
   * @returns {Promise<Object>}
   */
  async payBill(billId) {
    const msisdn = localStorage.getItem('userPhone');
    if (!msisdn) throw new Error('No user phone number');

    // Bills pay endpoint'i yok, simülatör başarı mesajı döndür
    console.warn('Bills pay endpoint not available, using simulator response');
    
    return {
      success: true,
      message: 'Payment successful',
      billId: billId
    };
  }

  /**
   * Get available packages
   * @returns {Promise<PackageResponse[]>}
   */
  async getPackages() {
    const response = await this.request('/api/v1/packages', { 
      method: 'GET',
      headers: { 'Device-Type': 'WEB' }
    });

    if (response.data) {
      return response.data;
    } else if (Array.isArray(response)) {
      return response;
    } else {
      throw new Error('Invalid packages response format');
    }
  }

  /**
   * Purchase a package
   * @param {number|string} packageId
   * @returns {Promise<Object>}
   */
  async purchasePackage(packageId) {
    const msisdn = localStorage.getItem('userPhone');
    if (!msisdn) throw new Error('No user phone number');

    return this.request('/api/v1/packages/purchase', {
      method: 'POST',
      headers: { 'Device-Type': 'WEB' },
      body: { 
        packageId,
        msisdn 
      },
    });
  }

  /**
   * Assign a package to a customer (package purchase)
   * @param {string|number} customerId
   * @param {string|number} packageId
   * @returns {Promise<Object>}
   */
  async assignPackageToCustomer(customerId, packageId) {
    return this.request(`/api/v1/customers/${customerId}/package/${packageId}`, {
      method: 'POST',
      headers: { 'Device-Type': 'WEB' }
    });
  }

  /**
   * Test API connection with existing users
   * @returns {Promise<Object>}
   */
  async testConnection() {
    try {
      console.log('Testing API connection...');
      console.log('Base URL:', this.baseURL);

      // Test users from database - Şifre kurallarına uygun
      const testUsers = [
        { msisdn: '5551234568', password: 'Test123!' }, // Yeni oluşturulan kullanıcı
        { msisdn: '5551234567', password: 'Test123!' },
        { msisdn: '5551234569', password: 'Test123!' },
        { msisdn: '5551234570', password: 'Test123!' },
        { msisdn: '5551234571', password: 'Test123!' },
        { msisdn: '5551234572', password: 'Test123!' }
      ];

      let successfulLogin = null;

      for (const user of testUsers) {
        try {
          console.log(`Testing login with: ${user.msisdn}`);
          const loginResponse = await this.request('/api/v1/auth/login', {
            method: 'POST',
            headers: { 'Device-Type': 'WEB' },
            body: user,
          });

          console.log(`Login successful for ${user.msisdn}:`, loginResponse);
          successfulLogin = { user, response: loginResponse };
          break;
        } catch (error) {
          console.log(`Login failed for ${user.msisdn}:`, error.message);
        }
      }

      if (!successfulLogin) {
        throw new Error('No valid user found in database');
      }

      // Test balance endpoint
      try {
        const balanceResponse = await this.request('/api/v1/balance', {
          method: 'POST',
          body: { msisdn: successfulLogin.user.msisdn },
        });
        console.log('Balance response:', balanceResponse);
      } catch (error) {
        console.log('Balance test failed:', error.message);
      }

      return { success: true, response: successfulLogin.response };
    } catch (error) {
      console.error('API test failed:', error);
      return { success: false, error: error.message };
    }
  }

  /**
   * Logout user and clear session
   * @returns {Promise<void>}
   */
  async logout() {
    try {
      await this.request('/api/v1/auth/logout', { 
        method: 'POST',
        headers: { 'Device-Type': 'WEB' }
      });
    } finally {
      localStorage.removeItem('userPhone');
      localStorage.removeItem('userId');
      localStorage.removeItem('userName');
      localStorage.removeItem('userSurname');
      localStorage.removeItem('userEmail');
      window.location.href = '/login';
    }
  }
}

// Export singleton instance
const apiService = new ApiService();

// Gemini API ayarları
const GEMINI_API_KEY = "AIzaSyBwPda1dECg3Yt3jRURWdutUqR7sC5u7RM"; 
const GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

const CELLENTA_SYSTEM_PROMPT = `
Sen Cellenta Bot'sunuz, Cellenta Online Charging System için sanal asistansın. Sadece Cellenta uygulaması hakkındaki sorulara cevap ver:

- Faturalama ve bakiye
- Kalan kullanım (internet, dakika, SMS)
- Abonelik paketleri
- CRM ve Sipariş Yönetimi
- Hesap girişi, kayıt ve şifre kurtarma

Kullanıcı başka bir konu hakkında soru sorarsa, kibar bir şekilde sadece Cellenta ile ilgili yardımcı olabileceğini söyle.

**ÖNEMLİ:**
- Her zaman SADECE Türkçe yanıt ver.
- Net, profesyonel ve kısa ol.
- Adım adım rehberlik et.

Giriş yaptıktan sonra uygulama şunları sağlar:
- Ana Sayfa: Kalan dakika, internet, SMS
- Mağaza: Paketleri görüntüle ve satın al
- Faturalar: Ödeme geçmişi
- Profil: Detayları görüntüle, çıkış yap

Kalan kullanım hakkında sorularsa ve hepsi sıfırsa şöyle söyle: "Kalan kullanımınız yok, tüm haklarınız bitmiş görünüyor."

**Kullanıcı paket değişikliği sorarsa ve zaten paket varsa:**
"Şu an zaten bir paketiniz var, paket değişikliği yapamazsınız. Paket alımı sadece yeni kullanıcılar için geçerlidir."

**Kullanıcının paketi yoksa ve paket almak istiyorsa, adım adım açıkla:**
"Paket almak için: 1. Uygulamaya giriş yapın. 2. 'Mağaza' bölümüne gidin. 3. Listeden bir paket seçin. 4. Satın alma işlemini tamamlayın."

**Giriş, kayıt veya şifre kurtarma sorularında adım adım açıkla:**
"Giriş yapmak için: 1. Telefon numaranızı ve şifrenizi girin. 2. 'Giriş Yap' butonuna tıklayın. Kayıt olmak için: 1. Ad, soyad, telefon ve e-posta bilgilerinizi girin. 2. Şifre oluşturun. 3. 'Kayıt Ol' butonuna tıklayın. Şifrenizi unuttuysanız: 1. Giriş ekranında 'Şifremi Unuttum' seçeneğine tıklayın. 2. E-posta adresinizi girin. 3. Gelen 6 haneli kodu girin ve yeni şifre belirleyin."

Her yanıtın sonunda şunu sor: "Başka bir konuda yardımcı olabilir miyim?"
`;

/**
 * Gemini (Google) ile Cellenta Bot'a prompt gönderir ve yanıt alır.
 * @param {string} prompt
 * @returns {Promise<string>}
 */
async function sendGeminiMessage(prompt) {
  const url = `${GEMINI_ENDPOINT}?key=${GEMINI_API_KEY}`;

  const body = {
    contents: [
      {
        parts: [
          { text: `${CELLENTA_SYSTEM_PROMPT}\n\nUser: ${prompt}` }
        ]
      }
    ]
  };

  const response = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    throw new Error(`Gemini API error: ${response.status}`);
  }

  const data = await response.json();
  if (
    data.candidates &&
    data.candidates[0] &&
    data.candidates[0].content &&
    data.candidates[0].content.parts &&
    data.candidates[0].content.parts[0] &&
    data.candidates[0].content.parts[0].text
  ) {
    return data.candidates[0].content.parts[0].text;
  } else {
    throw new Error("No response text found from Gemini API");
  }
}

export { sendGeminiMessage };
export default apiService; 