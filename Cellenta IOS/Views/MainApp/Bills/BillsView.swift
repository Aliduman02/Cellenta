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
    let daysLeft: String?
    let packageName: String?
}

// MARK: - Invoice Request Body
struct InvoiceRequest: Codable {
    let msisdn: String
}

import SwiftUI

struct BillsView: View {
    @Binding var selectedTab: Tab
    @State private var invoices: [Invoice] = []
    @State private var isLoading = true
    @State private var errorMessage: String?
    @State private var showingPaymentAlert = false

    // Calculate total unpaid amount
    private var totalUnpaidAmount: Double {
        invoices.filter { $0.paymentStatus == "UNPAID" }.reduce(0) { $0 + $1.price }
    }
    
    // Check if there are any unpaid invoices
    private var hasUnpaidInvoices: Bool {
        invoices.contains { $0.paymentStatus == "UNPAID" }
    }

    var body: some View {
        NavigationView {
            VStack {
                // MARK: - Navigation Bar
                HStack {
                    Image(systemName: "")
                        .font(.title2)
                        .foregroundColor(.black)

                    Text("Fatura Geçmişi")
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(.black)

                    Spacer()

                   
                }
                .padding(.horizontal)
                .padding(.top, 10)

                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        // MARK: - Unpaid Summary Section
                        if hasUnpaidInvoices && !isLoading {
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("Ödenmemiş Toplam")
                                        .font(.headline)
                                        .foregroundColor(.black)
                                    Text("\(String(format: "%.2f", totalUnpaidAmount)) TL")
                                        .font(.title2)
                                        .fontWeight(.bold)
                                        .foregroundColor(.red)
                                }
                                
                                Spacer()
                                
                                Button(action: {
                                    showingPaymentAlert = true
                                }) {
                                    Text("Hemen Öde")
                                        .font(.headline)
                                        .foregroundColor(.white)
                                        .padding(.vertical, 10)
                                        .padding(.horizontal, 20)
                                        .background(Color.blue)
                                        .cornerRadius(10)
                                }
                                .alert(isPresented: $showingPaymentAlert) {
                                    Alert(
                                        title: Text("Ödeme Yap"),
                                        message: Text("\(String(format: "%.2f", totalUnpaidAmount)) TL tutarındaki ödenmemiş faturalarınızı ödemek istiyor musunuz?"),
                                        primaryButton: .default(Text("Öde")) {
                                            // Handle payment logic here
                                            print("Payment initiated for \(totalUnpaidAmount) TL")
                                        },
                                        secondaryButton: .cancel()
                                    )
                                }
                            }
                            .padding()
                            .background(Color.white)
                            .cornerRadius(10)
                            .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
                            .padding(.horizontal)
                        }

                        
                        

                        // MARK: - Header
                        Text("Son Ödemeler")
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(.black)
                            .padding(.horizontal)
                            .padding(.top, 10)

                        // MARK: - Invoice List
                        if isLoading {
                            ProgressView("Fatura yükleniyor...")
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
                                            return ("Ödenmiş", .green, "checkmark.circle.fill")
                                        case "UNPAID":
                                            return ("Ödenmedi", .red, "xmark.circle.fill")
                                        default:
                                            return (invoice.daysLeft ?? "Bekliyor", .orange, "clock.fill")
                                        }
                                    }()
                                    
                                    PaymentHistoryRow(
                                        iconName: status.2,
                                        iconColor: status.1,
                                        dateRange: "\(invoice.startDate) - \(invoice.endDate)",
                                        status: status.0,
                                        amount: "\(Int(invoice.price)) TL",
                                        packageName: invoice.packageName ?? ""
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
                        let msisdn = UserSession.shared.msisdn
                        let result = try await InvoiceService.fetchInvoices(msisdn: msisdn)
                        invoices = result
                        isLoading = false
                    } catch let error as InvoiceServiceError {
                        errorMessage = error.localizedDescription
                        isLoading = false
                    } catch {
                        errorMessage = "Fatura yüklenirken beklenmeyen bir hata oluştu: \(error.localizedDescription)"
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
    let packageName: String

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
                Text("\(packageName) - \(status)")
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
