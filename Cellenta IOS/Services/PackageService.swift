//
//  PackageResponse.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 19.07.2025.
//
import Foundation

struct Package: Codable, Identifiable, Hashable {
    var id: Int { package_id }

    let package_id: Int
    let packageName: String
    let price: Double
    let amountMinutes: Int
    let amountData: Int
    let amountSms: Int
    let period: Int
}
struct PurchaseRequest: Codable {
    let packageId: Int
    let msisdn: String
}
class PackageService {
    static func fetchPackages() async throws -> [Package] {
        guard let url = URL(string: "http://34.123.86.69/api/v1/packages") else {
            throw URLError(.badURL)
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }

        return try JSONDecoder().decode([Package].self, from: data)
    }

    
    static func selectPackage(customerId: Int, packageId: Int, msisdn: String) async throws -> BalanceResponse {
        // Step 1: Purchase the package
        let purchaseURL = URL(string: "http://34.123.86.69/api/v1/customers/\(customerId)/package/\(packageId)")!
        var purchaseRequest = URLRequest(url: purchaseURL)
        purchaseRequest.httpMethod = "POST"
        purchaseRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")
        purchaseRequest.setValue("iOS", forHTTPHeaderField: "Device-Type")

        let (_, purchaseResponse) = try await URLSession.shared.data(for: purchaseRequest)

        guard let httpResponse = purchaseResponse as? HTTPURLResponse,
              (200...299).contains(httpResponse.statusCode) else {
            throw URLError(.badServerResponse)
        }
    

        // Step 2: Fetch updated balance
        let balanceURL = URL(string: "http://34.123.86.69/api/v1/balance")!
        var balanceRequest = URLRequest(url: balanceURL)
        balanceRequest.httpMethod = "POST"
        balanceRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let requestBody = ["msisdn": msisdn]
        balanceRequest.httpBody = try JSONSerialization.data(withJSONObject: requestBody)

        let (balanceData, balanceResponse) = try await URLSession.shared.data(for: balanceRequest)

        guard let balanceHttpResponse = balanceResponse as? HTTPURLResponse,
              balanceHttpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }

        return try JSONDecoder().decode(BalanceResponse.self, from: balanceData)
    }
    
    private func hasRemainingBalance(_ balance: BalanceResponse) -> Bool {
        return balance.remainingData > 0 ||
               balance.remainingMinutes > 0 ||
               balance.remainingSms > 0
    }
}
