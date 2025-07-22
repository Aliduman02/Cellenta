//
//  InoviceService.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 21.07.2025.
//
import Foundation

func fetchInvoices(msisdn: String) async throws -> [Invoice] {
    guard let url = URL(string: "http://34.123.86.69/api/v1/customers/invoices") else {
        throw URLError(.badURL)
    }

    var request = URLRequest(url: url)
    request.httpMethod = "POST"
    request.setValue("application/json", forHTTPHeaderField: "Content-Type")

    let requestBody = InvoiceRequest(msisdn: msisdn)
    request.httpBody = try JSONEncoder().encode(requestBody)

    let (data, response) = try await URLSession.shared.data(for: request)

    guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
        throw URLError(.badServerResponse)
    }

    return try JSONDecoder().decode([Invoice].self, from: data)
}
