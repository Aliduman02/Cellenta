//
//  AuthService.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 18.07.2025.
//

import Foundation


struct LoginRequest: Codable {
    let msisdn: String
    let password: String
}

struct LoginResponse: Codable {
    let cust_id: Int
    let msisdn: String
    let name: String
    let surname: String
    let email: String
    let sdate: String
}
enum AuthError: Error {
    case server(String)
    case unknown

    var message: String {
        switch self {
        case .server(let msg):
            return msg
        case .unknown:
            return "An unknown error occurred. Please try again."
        }
    }
}
class AuthService {
    static let shared = AuthService()
    private init() {}
    


    func login(msisdn: String, password: String) async throws -> LoginResponse {
        let url = URL(string: "http://34.123.86.69/api/v1/auth/login")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("IOS", forHTTPHeaderField: "Device-Type")

        let body = LoginRequest(msisdn: msisdn, password: password)
        request.httpBody = try JSONEncoder().encode(body)

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw URLError(.badServerResponse)
        }

        if httpResponse.statusCode == 200 {
            let decoded = try JSONDecoder().decode(LoginResponse.self, from: data)
            return decoded
        } else {
            let rawError = String(data: data, encoding: .utf8) ?? "Unknown error"
            throw NSError(domain: "", code: httpResponse.statusCode, userInfo: [NSLocalizedDescriptionKey: rawError])
        }
    }
    
    func verifyResetCode(email: String, code: String) async throws -> Bool {
        let url = URL(string: "http://34.123.86.69/api/v1/auth/verify-code")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let body = ["email": email, "code": code]
        request.httpBody = try JSONEncoder().encode(body)

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw URLError(.badServerResponse)
        }

        let responseBody = String(data: data, encoding: .utf8) ?? "No response"
        print("📡 Status Code: \(httpResponse.statusCode)")
        print("📦 Response Body: \(responseBody)")

        struct VerifyCodeResponse: Decodable {
            let message: String
            let error: String?
            let status: Int?
        }

        let decoded = try JSONDecoder().decode(VerifyCodeResponse.self, from: data)
        
        if decoded.message.lowercased() == "fail" {
            throw NSError(domain: "Verification", code: 1, userInfo: [
                NSLocalizedDescriptionKey: decoded.error ?? "Verification failed"
            ])
        }
        print("✅ Email verified successfully for \(email)")
        return true
    }
    
    func sendRecoveryEmail(email: String) async throws -> Bool {
            guard let url = URL(string: "http://34.123.86.69/api/v1/auth/forgot-password") else {
                throw URLError(.badURL)
            }

            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.setValue("IOS", forHTTPHeaderField: "Device-Type")

            let payload = ["email": email]
            request.httpBody = try JSONSerialization.data(withJSONObject: payload)

            let (data, response) = try await URLSession.shared.data(for: request)

            guard let httpResponse = response as? HTTPURLResponse else {
                throw URLError(.badServerResponse)
            }
            
            let responseText = String(data: data, encoding: .utf8) ?? "No response body"
            print("📡 Status Code: \(httpResponse.statusCode)")
            print("📦 Response Body: \(String(data: data, encoding: .utf8) ?? "No response")")
            
            if httpResponse.statusCode == 200 {
                print("✅ Recovery email sent to \(email)")
                return true
            } else {
                throw NSError(domain: "RecoveryEmail", code: httpResponse.statusCode, userInfo: [
                    NSLocalizedDescriptionKey: responseText
                ])
            }
            
        }
    
    func changePassword(email: String, password: String, verificationCode: String) async throws -> Bool {
        guard let url = URL(string: "http://34.123.86.69/api/v1/customers/change-password") else {
            throw URLError(.badURL)
        }

        var request = URLRequest(url: url)
        request.httpMethod = "PATCH"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let body: [String: String] = [
            "email": email,
            "password": password,
            "verificationCode": verificationCode
        ]
        request.httpBody = try JSONEncoder().encode(body)

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw URLError(.badServerResponse)
        }

        let responseText = String(data: data, encoding: .utf8) ?? "No response"
        print("📡 Change Password Status Code: \(httpResponse.statusCode)")
        print("📦 Response: \(responseText)")

        if httpResponse.statusCode == 200 {
            print("✅ Password changed for \(email)")
            return true
        } else {
            throw NSError(domain: "ChangePassword", code: httpResponse.statusCode, userInfo: [
                NSLocalizedDescriptionKey: responseText
            ])
        }
    }
}
