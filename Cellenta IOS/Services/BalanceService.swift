//
//  BalanceService.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 19.07.2025.
//
import Foundation

struct BalanceRequest: Codable {
    let msisdn: String
}

struct BalanceResponse: Codable {
    let remainingMinutes: Int
    let remainingData: Int
    let remainingSms: Int
    let sdate: String
    let edate: String
    let packageName: String
    let price: Double
    let amountMinutes: Int
    let amountData: Int
    let amountSms: Int
    let period: Int
}

class BalanceService {
    static func fetchBalance(msisdn: String) async throws -> BalanceResponse {
        guard let url = URL(string: "http://34.123.86.69/api/v1/balance") else {
            throw URLError(.badURL)
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("iOS", forHTTPHeaderField: "Device-Type") // ✅ Important header


        let requestBody = BalanceRequest(msisdn: msisdn)
        request.httpBody = try JSONEncoder().encode(requestBody)

        print("📤 Sending balance request for MSISDN: \(msisdn)")
        
        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            print("❌ Invalid HTTP response")
            throw URLError(.badServerResponse)
        }

        print("📥 Status code: \(httpResponse.statusCode)")
        let responseString = String(data: data, encoding: .utf8) ?? "N/A"
        print("📦 Response body: \(responseString)")

        guard httpResponse.statusCode == 200 else {
            print("❌ Server returned error status")
            throw URLError(.badServerResponse)
        }

        return try JSONDecoder().decode(BalanceResponse.self, from: data)
    }
}
