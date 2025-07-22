//
//  GeminiService.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 19.07.2025.
//
import Foundation

class GeminiService {
    static let shared = GeminiService()
    private init() {}

    private let apiKey = "AIzaSyD8uY_pMgQfI2uPETXXvYNONfpc9N7SWWw" // ← Replace with your actual key
    private let endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

    func sendMessage(prompt: String) async throws -> String {
        guard let url = URL(string: endpoint) else {
            throw URLError(.badURL)
        }

        let body: [String: Any] = [
            "system_instruction": [
                "role": "system",
                "parts": [
                    [
                        "text": """
                        You are Cellenta Bot, a virtual assistant exclusively for the Cellenta Online Charging System. You only respond to questions related to the Cellenta app, including:

                        - account access (login, signup, password recovery),
                        - billing and balance inquiries,
                        - remaining usage (data, minutes, SMS),
                        - subscription packages,
                        - SMS inquiries (e.g., sending ‘KALAN’ to 4848),
                        - CRM and Order Management-related support.

                        If a user asks a question that is not related to the Cellenta system, politely respond:
                        - In English: “This assistant is only able to help with questions related to the Cellenta app.”
                        - In Turkish: “Bu asistan sadece Cellenta uygulaması ile ilgili sorulara yardımcı olabilir.”

                        Do not provide general knowledge, entertainment, or personal advice. Stay professional, clear, and polite. Respond in Turkish or English depending on the user’s message.

                        Here is how the Cellenta app workflow functions:

                        1. If the user does **not have an account**, guide them to sign up using:
                           - First name (must be alphabetical and less than 60 characters)
                           - Last name  (must be alphabetical and less than 60 characters)
                           - Phone number (must start with 5 and be 10 digits total)
                           - Password   (must be more than 8 characters, consisting of at least one uppercase letter, one lowercase letter and one number)

                        2. If the user **has an account**, prompt them to log in with their:
                           - Phone number (must start with 5 and be 10 digits total)
                           - Password   (must be more than 8 characters, consisting of at least one uppercase letter, one lowercase letter and one number)

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
                        Use the following logic to recommend packages:

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

                        Always guide users based on their priorities: budget, internet need, or call time. Ask clarifying questions like:
                        - "Do you use more internet or minutes?"
                        - "Are you looking for the cheapest option or something more complete?"

                        Assume the user may be confused or unsure about which step comes next. Help them with patience.
                        """
                    ]
                ]
            ],
            "contents": [
                [
                    "parts": [
                        ["text": prompt]
                    ]
                ]
            ]
        ]

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(apiKey, forHTTPHeaderField: "X-goog-api-key")
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw URLError(.badServerResponse)
        }

        guard httpResponse.statusCode == 200 else {
            let bodyString = String(data: data, encoding: .utf8) ?? "No response body"
            throw NSError(domain: "GeminiAPI", code: httpResponse.statusCode, userInfo: [
                NSLocalizedDescriptionKey: "Status code: \(httpResponse.statusCode)\nResponse: \(bodyString)"
            ])
        }

        let decoded = try JSONDecoder().decode(GeminiResponse.self, from: data)
        return decoded.candidates.first?.content.parts.first?.text ?? "No reply."
    }
}

// MARK: - Response Models
struct GeminiResponse: Codable {
    let candidates: [Candidate]
}

struct Candidate: Codable {
    let content: Content
}

struct Content: Codable {
    let parts: [Part]
}

struct Part: Codable {
    let text: String
}
