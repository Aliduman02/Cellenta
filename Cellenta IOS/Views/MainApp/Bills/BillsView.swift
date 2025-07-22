//
//  BillsView.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 17.07.2025.
//
// MARK: - Invoice Model
struct Invoice: Codable, Identifiable {
    let id: Int
    let customerId: Int
    let packageId: Int
    let startDate: String
    let endDate: String
    let price: Double
    let paymentStatus: String
    let isActive: String
    let daysLeft: String
}

// MARK: - Invoice Request Body
struct InvoiceRequest: Codable {
    let msisdn: String
}


import SwiftUI

struct BillsView: View {
    @State private var selectedMonth: String = "Mar 2021"
    @Binding var selectedTab: Tab

    @State private var invoices: [Invoice] = []
    @State private var isLoading = true
    @State private var errorMessage: String?

    var body: some View {
        NavigationView {
            VStack {
                // MARK: - Navigation Bar
                HStack {
                    Image(systemName: "")
                        .font(.title2)
                        .foregroundColor(.black)

                    Text("Bills History")
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(.black)

                    Spacer()

                    // Month Dropdown
                    Menu {
                        Button("Mar 2021") { selectedMonth = "Mar 2021" }
                        Button("Feb 2021") { selectedMonth = "Feb 2021" }
                        Button("Jan 2021") { selectedMonth = "Jan 2021" }
                    } label: {
                        HStack {
                            Text(selectedMonth)
                                .font(.subheadline)
                                .foregroundColor(.black)
                            Image(systemName: "chevron.down")
                                .font(.caption)
                                .foregroundColor(.black)
                        }
                        .padding(.vertical, 8)
                        .padding(.horizontal, 12)
                        .background(Color.gray.opacity(0.1))
                        .cornerRadius(8)
                    }
                }
                .padding(.horizontal)
                .padding(.top, 10)

                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {

                        // MARK: - Current Payment Placeholder
                        HStack {
                            VStack(alignment: .leading) {
                                Text("")
                                    .font(.title2)
                                    .fontWeight(.bold)
                                    .foregroundColor(.black)
                                Text("")
                                    .font(.caption)
                                    .foregroundColor(.gray)
                            }
                            Spacer()
                            Image(systemName: "arrow.right")
                                .font(.title2)
                                .foregroundColor(Color(red: 0.2, green: 0.6, blue: 0.7))
                        }
                        .padding()
                        .background(Color.white)
                        .cornerRadius(10)
                        .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
                        .padding(.horizontal)

                        // MARK: - Header
                        Text("Last Payments")
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(.black)
                            .padding(.horizontal)
                            .padding(.top, 10)

                        // MARK: - Invoice List
                        if isLoading {
                            ProgressView("Loading invoices...")
                                .padding()
                        } else if let errorMessage = errorMessage {
                            Text(errorMessage)
                                .foregroundColor(.red)
                                .padding()
                        } else {
                            VStack(spacing: 15) {
                                ForEach(invoices) { invoice in
                                    let status: (String, Color, String) = {
                                        switch invoice.paymentStatus {
                                        case "PAID":
                                            return ("Paid", .green, "checkmark.circle.fill")
                                        case "LATE":
                                            return ("Late", .red, "xmark.circle.fill")
                                        default:
                                            return (invoice.daysLeft, .orange, "clock.fill")
                                        }
                                    }()
                                    PaymentHistoryRow(
                                        iconName: status.2,
                                        iconColor: status.1,
                                        dateRange: "\(invoice.startDate) - \(invoice.endDate)",
                                        status: status.0,
                                        amount: "\(Int(invoice.price)) TL"
                                    )
                                }
                            }
                            .padding(.horizontal)
                        }

                        Spacer()
                    }
                }
                .background(Color.white.edgesIgnoringSafeArea(.all))
            }
            .navigationBarHidden(true)
            .onAppear {
                Task {
                    do {
                        let msisdn = UserSession.shared.msisdn // no if-let
                        let result = try await fetchInvoices(msisdn: msisdn)
                        invoices = result
                        isLoading = false
                    } catch {
                        errorMessage = "Failed to load invoices."
                        isLoading = false
                    }
                }
            }
        }
    }
}
// MARK: - PaymentHistoryRow
struct PaymentHistoryRow: View {
    let iconName: String
    let iconColor: Color
    let dateRange: String
    let status: String
    let amount: String

    var body: some View {
        HStack(spacing: 15) {
            Image(systemName: iconName)
                .font(.title2)
                .foregroundColor(iconColor)
                .frame(width: 30, height: 30)

            VStack(alignment: .leading, spacing: 4) {
                Text(dateRange)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(.black)
                Text(status)
                    .font(.caption)
                    .foregroundColor(.gray)
            }

            Spacer()

            Text(amount)
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(.black)
        }
        .padding(.vertical, 8)
    }
}

