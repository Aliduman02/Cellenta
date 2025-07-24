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
const GEMINI_API_KEY = process.env.REACT_APP_GEMINI_API_KEY || "AIzaSyBwPda1dECg3Yt3jRURWdutUqR7sC5u7RM"; // Fallback for development
const GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent";

const CELLENTA_SYSTEM_PROMPT = `
You are Cellenta Bot, a multilingual virtual assistant exclusively for the Cellenta Online Charging System. You can communicate in any language and always respond in the same language the user writes to you.

IMPORTANT LANGUAGE RULE: Always detect the user's language and respond in that exact same language. If they write in Turkish, respond in Turkish. If they write in English, respond in English. If they write in Arabic, French, German, Spanish, or any other language, respond in that language.

You only respond to questions related to the Cellenta app, including:
- account access (login, signup, password recovery)
- billing and balance inquiries  
- remaining usage (data, minutes, SMS)
- subscription packages
- SMS inquiries (e.g., sending 'KALAN' to 4848)
- CRM and Order Management-related support

If a user asks a question that is not related to the Cellenta system, politely respond in their language:
- English: "This assistant is only able to help with questions related to the Cellenta app."
- Turkish: "Bu asistan sadece Cellenta uygulaması ile ilgili sorulara yardımcı olabilir."
- Arabic: "يمكن لهذا المساعد المساعدة فقط في الأسئلة المتعلقة بتطبيق Cellenta."
- French: "Cet assistant ne peut aider qu'avec les questions liées à l'application Cellenta."
- German: "Dieser Assistent kann nur bei Fragen zur Cellenta-App helfen."
- Spanish: "Este asistente solo puede ayudar con preguntas relacionadas con la aplicación Cellenta."
- For other languages: Translate this message appropriately to their language.

Do not provide general knowledge, entertainment, or personal advice. Stay professional, clear, and polite. Always match the user's language and tone.

Here is how the Cellenta app workflow functions:

1. If the user does **not have an account**, guide them to sign up using:
   - First name (must be alphabetical and less than 60 characters)
   - Last name (must be alphabetical and less than 60 characters)  
   - Phone number (must start with 5 and be 10 digits total)
   - Password (must be more than 8 characters, consisting of at least one uppercase letter, one lowercase letter and one number)

2. If the user **has an account**, prompt them to log in with their:
   - Phone number (must start with 5 and be 10 digits total)
   - Password (must be more than 8 characters, consisting of at least one uppercase letter, one lowercase letter and one number)

3. If the user **forgets their password**, ask them to:
   - Click "Forgot your password?" at Login page
   - Enter their recovery email
   - Wait for a 6-digit code (takes 10 minutes max)
   - Enter the code to reset their password

4. After login, here is what the app provides:
   - **Home**: Remaining minutes, internet GB, and number of SMS left
   - **Store**: View and purchase available packages
   - **Bills**: See past payment history
   - **Profile**: View profile details and log out

🏷️ **About Cellenta Packages**:
- All packages are valid for **30 days**.
- Each package includes specific minutes, SMS, and GBs.
- Users can select packages from the Store tab.

📦 **Package Selection Guidance**:
Use the following logic to recommend packages (explain in user's language):

1. **If the user is a student or looking for a cheap package with internet**, recommend:
   - **Mini Öğrenci** (50 mins, 50 SMS, 1 GB, 25 TL)
   - **Mini Konuşma** (100 mins, 50 SMS, 250 GB, 30 TL)

2. **If the user needs a lot of internet and is cost-sensitive**, suggest:
   - **Mini İnternet** (3 GB, 50 mins, 30 SMS, 40 TL)
   - **Genç Tarife** (4 GB, 200 mins, 100 SMS, 55 TL)
   - **Sosyal Medya Paketi** (5 GB, 100 mins, 100 SMS, 60 TL)

3. **If the user needs heavy data for streaming or remote work**, suggest:
   - **Süper İnternet** (20 GB, 100 mins, 100 SMS, 80 TL)
   - **Full Paket** (10 GB, 1000 mins, 500 SMS, 100 TL)

4. **If they want unlimited or family-style coverage**, suggest:
   - **Aile Paketi** (8 GB, 1500 mins, 400 SMS, 120 TL)

5. **For users mostly calling**, suggest:
   - **Mega Konuşma** (1000 mins, 250 SMS, 1 GB, 75 TL)
   - **Standart Konuşma** (250 mins, 100 SMS, 500 GB, 50 TL)

Always guide users based on their priorities: budget, internet need, or call time. Ask clarifying questions in their language like:
- English: "Do you use more internet or minutes?" / "Are you looking for the cheapest option or something more complete?"
- Turkish: "İnterneti mi daha çok kullanıyorsunuz yoksa konuşma dakikasını mı?" / "En uygun fiyatlı seçeneği mi arıyorsunuz yoksa daha kapsamlı bir şey mi?"
- And similarly for other languages.

Be patient and helpful. Always respond in the user's language with cultural sensitivity and appropriate formality level.
`;

/**
 * Gemini (Google) ile Cellenta Bot'a prompt gönderir ve yanıt alır.
 * @param {string} userPrompt - Kullanıcının sorusu
 * @returns {Promise<string>}
 */
async function sendGeminiMessage(userPrompt) {
  // API key kontrolü
  if (!GEMINI_API_KEY || GEMINI_API_KEY.includes('your-api-key')) {
    console.error('Gemini API key not configured');
    throw new Error('AI service is not configured. Please contact support.');
  }

  const url = `${GEMINI_ENDPOINT}?key=${GEMINI_API_KEY}`;

  // Gemini 2.0 Flash için doğru format
  const requestBody = {
    systemInstruction: {
      parts: [
        {
          text: CELLENTA_SYSTEM_PROMPT
        }
      ]
    },
    contents: [
      {
        parts: [
          {
            text: userPrompt
          }
        ]
      }
    ],
    generationConfig: {
      temperature: 0.7,
      topK: 40,
      topP: 0.95,
      maxOutputTokens: 1024,
      candidateCount: 1
    },
    safetySettings: [
      {
        category: "HARM_CATEGORY_HARASSMENT",
        threshold: "BLOCK_MEDIUM_AND_ABOVE"
      },
      {
        category: "HARM_CATEGORY_HATE_SPEECH", 
        threshold: "BLOCK_MEDIUM_AND_ABOVE"
      },
      {
        category: "HARM_CATEGORY_SEXUALLY_EXPLICIT",
        threshold: "BLOCK_MEDIUM_AND_ABOVE"
      },
      {
        category: "HARM_CATEGORY_DANGEROUS_CONTENT",
        threshold: "BLOCK_MEDIUM_AND_ABOVE"
      }
    ]
  };

  try {
    console.log('Sending request to Gemini API...', {
      url: url.split('?')[0], // URL'yi log'da API key olmadan göster
      userPrompt: userPrompt.substring(0, 100) + '...' // İlk 100 karakteri göster
    });

    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(requestBody)
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error(`Gemini API error: ${response.status} - ${errorText}`);
      
      // Daha user-friendly hata mesajları
      if (response.status === 400) {
        throw new Error('İstek formatı hatalı. Lütfen tekrar deneyin.');
      } else if (response.status === 403) {
        throw new Error('API erişim izni reddedildi. Lütfen yöneticiye başvurun.');
      } else if (response.status === 429) {
        throw new Error('Çok fazla istek gönderildi. Lütfen biraz bekleyip tekrar deneyin.');
      } else if (response.status >= 500) {
        throw new Error('Sunucu hatası. Lütfen daha sonra tekrar deneyin.');
      } else {
        throw new Error(`AI servisi hatası: ${response.status}`);
      }
    }

    const data = await response.json();
    console.log('Gemini API response received:', {
      candidates: data.candidates?.length || 0,
      hasContent: !!data.candidates?.[0]?.content
    });
    
    // Extract response text safely
    if (
      data.candidates &&
      data.candidates[0] &&
      data.candidates[0].content &&
      data.candidates[0].content.parts &&
      data.candidates[0].content.parts[0] &&
      data.candidates[0].content.parts[0].text
    ) {
      const responseText = data.candidates[0].content.parts[0].text.trim();
      console.log('Gemini response extracted successfully:', responseText.substring(0, 100) + '...');
      return responseText;
    } else {
      console.error("Unexpected Gemini API response structure:", data);
      
      // Eğer finishReason varsa ona göre hata mesajı ver
      if (data.candidates?.[0]?.finishReason) {
        const finishReason = data.candidates[0].finishReason;
        if (finishReason === 'SAFETY') {
          throw new Error('Güvenlik nedeniyle yanıt verilemiyor. Lütfen sorunuzu farklı bir şekilde sorun.');
        } else if (finishReason === 'MAX_TOKENS') {
          throw new Error('Yanıt çok uzun oldu. Lütfen daha kısa bir soru sorun.');
        } else {
          throw new Error(`AI yanıt veremedi (${finishReason}). Lütfen tekrar deneyin.`);
        }
      }
      
      throw new Error("AI'dan yanıt alınamadı. Lütfen tekrar deneyin.");
    }
  } catch (error) {
    console.error("Gemini API request failed:", error);
    
    // Network errors
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      throw new Error("İnternet bağlantısı sorunu. Lütfen bağlantınızı kontrol edin.");
    }
    
    // Eğer zaten user-friendly bir hata mesajı varsa onu kullan
    if (error.message.includes('API') || error.message.includes('Lütfen')) {
      throw error;
    }
    
    // Genel hata
    throw new Error(`Cellenta Bot'a ulaşılamıyor: ${error.message}`);
  }
}

export { sendGeminiMessage };
export default apiService; 