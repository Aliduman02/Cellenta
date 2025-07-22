//
//  StoreView.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 17.07.2025.
//
import SwiftUI

struct StoreView: View {
    @Binding var selectedTab: Tab
    @Binding var forceRefresh: Bool
    @State private var selectedPackageIndex: Int?
    @State private var packages: [Package] = []
    @State private var isLoading = true
    @State private var errorMessage: String?
    @State private var hasActivePackage = false
    @State private var currentBalance: BalanceResponse?

    @AppStorage("msisdn") var msisdn: String = ""
    @AppStorage("cust_id") var cust_id: Int = 0  // ← This is 0!

    var body: some View {
        VStack(spacing: 0) {
            // MARK: - Navigation Bar
            HStack {
                Image(systemName: "chevron.left")
                    .font(.title2)
                    .foregroundColor(.black)
                Text("Store")
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(.black)
                Spacer()
            }
            .padding(.horizontal, 24)
            .padding(.top, 10)

            // MARK: - Active Package Status Banner
            if hasActivePackage {
                activePackageStatusBanner
            }

            // MARK: - Package List
            if isLoading {
                ProgressView("Loading packages...")
                    .frame(maxHeight: .infinity)
            } else if let errorMessage = errorMessage {
                Text(errorMessage)
                    .foregroundColor(.red)
                    .padding()
                Spacer()
            } else {
                ScrollView {
                    VStack(spacing: 15) {
                        ForEach(packages.indices, id: \.self) { index in
                            let pkg = packages[index]
                            PackageCardView(
                                package: pkg,
                                isSelected: selectedPackageIndex == index,
                                onSelect: {
                                    withAnimation(.easeOut(duration: 0.2)) {
                                        selectedPackageIndex = (selectedPackageIndex == index) ? nil : index
                                    }
                                },
                                forceRefresh: $forceRefresh,
                                hasActivePackage: hasActivePackage
                            )
                        }
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 10)
                }
            }

            Spacer().frame(height: 100)
        }
        .background(Color.white.edgesIgnoringSafeArea(.all))
        .navigationBarHidden(true)
        .task {
            await loadPackages()
        }
        .onChange(of: forceRefresh) { _ in
            Task { await loadPackages() }
        }
    }

    private var activePackageStatusBanner: some View {
        VStack(spacing: 12) {
            // Package Title and Status
            HStack {
                Image(systemName: "checkmark.circle.fill")
                    .foregroundColor(.green)
                VStack(alignment: .leading, spacing: 2) {
                    Text("Active Package")
                        .font(.subheadline)
                        .fontWeight(.medium)
                    if let balance = currentBalance {
                        Text(balance.packageName)
                            .font(.caption)
                            .foregroundColor(.gray)
                    }
                }
                Spacer()
            }
            
            if let balance = currentBalance {
                // Remaining Balance
                VStack(spacing: 8) {
                    HStack {
                        Text("Remaining:")
                            .font(.caption)
                            .foregroundColor(.gray)
                        Spacer()
                    }
                    
                    HStack(spacing: 16) {
                        balanceItem(icon: "wifi", value: "\(balance.remainingData)MB", total: "\(balance.amountData)GB", color: .blue)
                        balanceItem(icon: "phone", value: "\(balance.remainingMinutes)min", total: "\(balance.amountMinutes)min", color: .green)
                        balanceItem(icon: "message", value: "\(balance.remainingSms)", total: "\(balance.amountSms)", color: .orange)
                    }
                    
                    // Package Expiry Info
                    HStack {
                        Image(systemName: "calendar")
                            .foregroundColor(.gray)
                            .font(.caption)
                        Text("Expires: \(formatDate(balance.edate))")
                            .font(.caption)
                            .foregroundColor(.gray)
                        Spacer()
                        Text("\(String(format: "%.2f", balance.price)) TL")
                            .font(.caption)
                            .fontWeight(.medium)
                    }
                }
            }
        }
        .padding()
        .background(Color.green.opacity(0.1))
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.green.opacity(0.3), lineWidth: 1)
        )
        .padding(.horizontal)
        .padding(.vertical, 5)
    }
    
    private func balanceItem(icon: String, value: String, total: String, color: Color) -> some View {
        VStack(spacing: 4) {
            Image(systemName: icon)
                .foregroundColor(color)
                .font(.caption)
            Text(value)
                .font(.caption)
                .fontWeight(.bold)
            Text("of \(total)")
                .font(.system(size: 10))
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity)
    }
    
    private func formatDate(_ dateString: String) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss" // Adjust based on your API format
        
        if let date = formatter.date(from: dateString) {
            let displayFormatter = DateFormatter()
            displayFormatter.dateFormat = "MMM dd, HH:mm"
            return displayFormatter.string(from: date)
        }
        return dateString
    }

    @MainActor
    private func loadPackages() async {
        isLoading = true
        errorMessage = nil // Clear any previous errors
        
        do {
            packages = try await PackageService.fetchPackages()
            print("✅ Packages loaded successfully: \(packages.count) packages")
        } catch {
            errorMessage = "Failed to load packages."
            print("❌ Error loading packages: \(error.localizedDescription)")
            isLoading = false
            return
        }
        
        // Check for active package by trying to fetch balance
        do {
            currentBalance = try await BalanceService.fetchBalance(msisdn: msisdn)
            // If balance fetch succeeds, user has an active package
            hasActivePackage = hasRemainingBalance(currentBalance!)
            print("✅ Active package found: \(currentBalance?.packageName ?? "Unknown")")
        } catch {
            // If balance fetch fails (500 error), user has no active package - this is normal
            hasActivePackage = false
            currentBalance = nil
            print("ℹ️ No active package found (this is normal for new users): \(error.localizedDescription)")
        }
        
        isLoading = false
        print("🏪 Store loaded - hasActivePackage: \(hasActivePackage), packagesCount: \(packages.count)")
    }
    
    private func hasRemainingBalance(_ balance: BalanceResponse) -> Bool {
        // Consider package active if user has any remaining resources
        return balance.remainingData > 0 ||
               balance.remainingMinutes > 0 ||
               balance.remainingSms > 0
    }
}

struct PackageCardView: View {
    let package: Package
    let isSelected: Bool
    let onSelect: () -> Void
    @Binding var forceRefresh: Bool
    let hasActivePackage: Bool

    @AppStorage("customerId") var customerId: Int = 0
    @AppStorage("msisdn") var msisdn: String = ""
    @State private var isActivating = false
    @State private var activationMessage: String?

    var body: some View {
        Button(action: onSelect) {
            VStack(alignment: .leading, spacing: 10) {
                packageHeader

                if isSelected {
                    packageDetails
                    activateButton
                }
            }
            .padding()
            .background(hasActivePackage ? Color.gray.opacity(0.05) : Color.white)
            .cornerRadius(15)
            .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
            .overlay(
                RoundedRectangle(cornerRadius: 15)
                    .stroke(
                        hasActivePackage ? Color.gray.opacity(0.3) :
                        (isSelected ? Color.purple.opacity(0.6) : Color.gray.opacity(0.2)),
                        lineWidth: 1
                    )
            )
        }
        .buttonStyle(PlainButtonStyle())
        .disabled(hasActivePackage && !isSelected) // Allow tapping to view details, but disable selection
        .alert("Package Activation", isPresented: .constant(activationMessage != nil)) {
            Button("OK") { activationMessage = nil }
        } message: {
            Text(activationMessage ?? "")
        }
    }

    private var packageHeader: some View {
        HStack {
            VStack(alignment: .leading) {
                HStack {
                    Text(package.packageName)
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundColor(hasActivePackage ? .gray : .black)
                    
                    if hasActivePackage {
                        Image(systemName: "lock.circle.fill")
                            .foregroundColor(.gray)
                            .font(.caption)
                    }
                }

                if !isSelected {
                    Text("\(package.amountMinutes) dk \(package.amountSms) sms \(package.amountData) GB")
                        .font(.subheadline)
                        .foregroundColor(.gray)
                }
            }
            Spacer()
            Image(systemName: "arrow.forward")
                .font(.title2)
                .foregroundColor(hasActivePackage ? Color.gray.opacity(0.5) : Color.purple.opacity(0.6))
                .rotationEffect(.degrees(isSelected ? 90 : 0))
        }
    }

    private var packageDetails: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text("\(package.amountMinutes) Minutes")
            Text("\(package.amountSms) SMS")
            Text("\(package.amountData) GB")
            Text("Period: \(package.period) days")
            Text(String(format: "%.2f TL", package.price))
        }
        .font(.subheadline)
        .foregroundColor(hasActivePackage ? .gray : .black)
    }

    private var activateButton: some View {
        Button(action: {
            print("🔘 Activate button tapped - hasActivePackage: \(hasActivePackage)")
            if hasActivePackage {
                activationMessage = "❌ You already have an active package. Please wait until it expires or is fully consumed before selecting a new one."
            } else {
                print("🚀 Starting package activation for: \(package.packageName)")
                activatePackage()
            }
        }) {
            Group {
                if isActivating {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                        .padding()
                } else {
                    HStack {
                        if hasActivePackage {
                            Image(systemName: "lock.fill")
                        }
                        Text(hasActivePackage ? "Package Locked" : "Select Tariff")
                    }
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding()
                    .frame(maxWidth: .infinity)
                    .background(hasActivePackage ? Color.gray : Color.purple.opacity(0.6))
                    .cornerRadius(10)
                }
            }
        }
        .disabled(hasActivePackage)
    }

    private func activatePackage() {
        Task {
            isActivating = true
            defer { isActivating = false }

            do {
                print("📦 Activating package: \(package.packageName) for customer: \(customerId), MSISDN: \(msisdn)")
                
                let newBalance = try await PackageService.selectPackage(
                    customerId: customerId,
                    packageId: package.package_id,
                    msisdn: msisdn
                )
                
                // Create detailed success message
                let successMessage = """
                ✅ \(newBalance.packageName) activated successfully!
                
                📅 Valid until: \(formatActivationDate(newBalance.edate))
                💰 Price: \(String(format: "%.2f", newBalance.price)) TL
                
                Your New Balance:
                📱 \(newBalance.remainingData)MB Data
                📞 \(newBalance.remainingMinutes) Minutes  
                💬 \(newBalance.remainingSms) SMS
                """
                
                activationMessage = successMessage
                forceRefresh.toggle()
                
                print("✅ Package Activated Successfully - \(newBalance.packageName)")
                print("📦 New Balance: \(newBalance.remainingData)MB, \(newBalance.remainingMinutes)min, \(newBalance.remainingSms)SMS")
            } catch {
                let errorMsg = "❌ Failed to activate package: \(error.localizedDescription)"
                activationMessage = errorMsg
                print("❌ Package activation failed: \(error)")
            }
        }
    }
    
    private func formatActivationDate(_ dateString: String) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss" // Adjust based on your API format
        
        if let date = formatter.date(from: dateString) {
            let displayFormatter = DateFormatter()
            displayFormatter.dateFormat = "MMM dd, yyyy 'at' HH:mm"
            return displayFormatter.string(from: date)
        }
        return dateString
    }
}

