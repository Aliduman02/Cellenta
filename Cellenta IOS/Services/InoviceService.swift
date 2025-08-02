//
//  InoviceService.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 21.07.2025.
//

import Foundation

enum InvoiceServiceError: Error, LocalizedError {
    case invalidURL
    case invalidResponse
    case statusCode(Int)
    case decodingError(Error)
    case networkError(Error)
    case emptyResponse
    
    var errorDescription: String? {
        switch self {
        case .invalidURL: return "Invalid server URL"
        case .invalidResponse: return "Invalid response from server"
        case .statusCode(let code): return "Server error: \(code)"
        case .decodingError(let error): return "Data format error: \(error.localizedDescription)"
        case .networkError(let error): return "Network problem: \(error.localizedDescription)"
        case .emptyResponse: return "Server returned no data"
        }
    }
}

struct InvoiceService {
    static let baseURL = "http://34.123.86.69/api/v1/customers/invoices"
    static let timeoutInterval: TimeInterval = 30
    
    static func fetchInvoices(msisdn: String) async throws -> [Invoice] {
        guard let url = URL(string: baseURL) else {
            throw InvoiceServiceError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.timeoutInterval = timeoutInterval
        
        let requestBody = InvoiceRequest(msisdn: msisdn)
        
        do {
            request.httpBody = try JSONEncoder().encode(requestBody)
        } catch {
            throw InvoiceServiceError.decodingError(error)
        }
        
        do {
            let (data, _) = try await URLSession.shared.data(for: request)
            
            // Debug print the raw JSON
            if let jsonString = String(data: data, encoding: .utf8) {
                print("Raw JSON response:", jsonString)
            }
            
            let decoder = JSONDecoder()
            // Add custom date decoding strategy if needed
            // decoder.dateDecodingStrategy = .formatted(dateFormatter)
            
            return try decoder.decode([Invoice].self, from: data)
        } catch let decodingError as DecodingError {
            print("Detailed decoding error:", decodingError)
            throw InvoiceServiceError.decodingError(decodingError)
        } catch {
            throw InvoiceServiceError.networkError(error)
        }
    }
}
