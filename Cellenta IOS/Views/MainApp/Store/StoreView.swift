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
    @State private var recommendedPackages: [Package] = []
    

    @AppStorage("msisdn") var msisdn: String = ""
    @AppStorage("cust_id") var cust_id: Int = 0
    @AppStorage("firstName") var firstName: String = ""

    var body: some View {
        VStack(spacing: 0) {
            // MARK: - Navigation Bar
            HStack {
                Image(systemName: "chevron.left")
                    .font(.title2)
                    .foregroundColor(.black)
                Text("Mağaza")//Store
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
                ProgressView("Paketler yükleniyor...")//Loading packages...
                    .frame(maxHeight: .infinity)
            } else if let errorMessage = errorMessage {
                Text(errorMessage)
                    .foregroundColor(.red)
                    .padding()
                Spacer()
            } else {
                ScrollView {
                    VStack(spacing: 15) {
                        // MARK: - Recommended Packages Section
                        if !recommendedPackages.isEmpty {
                            recommendedPackagesSection
                        }
                        
                        // MARK: - All Packages Section
                        allPackagesSection
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
    
    // MARK: - Recommended Packages Section
    private var recommendedPackagesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "star.fill")
                    .foregroundColor(.orange)
                Text("Sizin İçin Önerilen Paketler")
                    .font(.headline)
                    .fontWeight(.bold)
                Spacer()
            }
            .padding(.horizontal, 4)
            
            ForEach(recommendedPackages.indices, id: \.self) { index in
                let pkg = recommendedPackages[index]
                if let packageIndex = packages.firstIndex(where: { $0.package_id == pkg.package_id }) {
                    PackageCardView(
                        package: pkg,
                        isSelected: selectedPackageIndex == packageIndex,
                        onSelect: {
                            withAnimation(.easeOut(duration: 0.2)) {
                                selectedPackageIndex = (selectedPackageIndex == packageIndex) ? nil : packageIndex
                            }
                        },
                        forceRefresh: $forceRefresh,
                        hasActivePackage: hasActivePackage,
                        isRecommended: true
                    )
                }
            }
            
            // Divider
            Divider()
                .padding(.vertical, 8)
            
            HStack {
                Text("Tüm Paketler")
                    .font(.headline)
                    .fontWeight(.bold)
                Spacer()
            }
            .padding(.horizontal, 4)
        }
    }
    
    // MARK: - All Packages Section
    private var allPackagesSection: some View {
        VStack(spacing: 15) {
            ForEach(packages.indices, id: \.self) { index in
                let pkg = packages[index]
                let isRecommended = recommendedPackages.contains { $0.package_id == pkg.package_id }
                
                if !isRecommended { // Only show non-recommended packages here
                    PackageCardView(
                        package: pkg,
                        isSelected: selectedPackageIndex == index,
                        onSelect: {
                            withAnimation(.easeOut(duration: 0.2)) {
                                selectedPackageIndex = (selectedPackageIndex == index) ? nil : index
                            }
                        },
                        forceRefresh: $forceRefresh,
                        hasActivePackage: hasActivePackage,
                        isRecommended: false
                    )
                }
            }
        }
    }

    private var activePackageStatusBanner: some View {
        VStack(spacing: 12) {
            // Package Title and Status
            HStack {
                Image(systemName: "checkmark.circle.fill")
                    .foregroundColor(.green)
                VStack(alignment: .leading, spacing: 2) {
                    Text("Aktif Paket")//"Active Package"
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
                        Text("Kalan:")//"Remaining:"
                            .font(.caption)
                            .foregroundColor(.gray)
                        Spacer()
                    }
                    
                    HStack(spacing: 16) {
                        balanceItem(
                            icon: "wifi",
                            value: String(format: "%.3f GB", Double(balance.remainingData) / 1000.0),
                            total: "\(balance.amountData / 1000) GB", // No decimals here
                            color: .blue
                        )
                        balanceItem(icon: "phone", value: "\(balance.remainingMinutes)dk", total: "\(balance.amountMinutes)dk", color: .green)//min
                        balanceItem(icon: "message", value: "\(balance.remainingSms)sms", total: "\(balance.amountSms)sms", color: .orange)
                    }
                    
                    // Package Expiry Info
                    HStack {
                        Image(systemName: "calendar")
                            .foregroundColor(.gray)
                            .font(.caption)
                        Text("Bitiyor: \(formatDate(balance.edate))")//Expires:
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
            print("✅ Paket yükleme başarılı: \(packages.count) paket")//"✅ Packages loaded successfully: \(packages.count) packages"
        } catch {
            errorMessage = "Paket yükleme başarısız"//"Failed to load packages."
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
        
        // Generate recommendations
        await generateRecommendations()
        
        isLoading = false
        print("🏪 Store loaded - hasActivePackage: \(hasActivePackage), packagesCount: \(packages.count)")
    }
    
    private func hasRemainingBalance(_ balance: BalanceResponse) -> Bool {
        // Consider package active if user has any remaining resources
        return balance.remainingData > 0 ||
               balance.remainingMinutes > 0 ||
               balance.remainingSms > 0
    }
    
    // MARK: - Package Recommendation Logic
    @MainActor
    private func generateRecommendations() async {
        guard !packages.isEmpty else { return }
        
        // If user has no active package, recommend starter packages
        if !hasActivePackage {
            recommendedPackages = getStarterRecommendations()
            return
        }
        
        // If user has active package but low resources, recommend based on usage pattern
        guard let balance = currentBalance else { return }
        
        // Get AI-powered recommendations
        do {
            let aiRecommendations = try await getAIRecommendations(balance: balance)
            recommendedPackages = aiRecommendations
        } catch {
            print("❌ AI recommendations failed, using fallback logic: \(error)")
            // Fallback to rule-based recommendations
            recommendedPackages = getRuleBasedRecommendations(balance: balance)
        }
    }
    
    private func getStarterRecommendations() -> [Package] {
        // Recommend budget-friendly packages for new users
        let starterPackageNames = ["Mini Öğrenci", "Mini Konuşma", "Mini İnternet"]
        return packages.filter { starterPackageNames.contains($0.packageName) }
                    .sorted { $0.price < $1.price }
                    .prefix(2)
                    .map { $0 }
    }
    
    private func getRuleBasedRecommendations(balance: BalanceResponse) -> [Package] {
        var recommendations: [Package] = []
        
        // Calculate usage percentages
        let dataUsagePercent = Double(balance.amountData - balance.remainingData) / Double(balance.amountData) * 100
        let minutesUsagePercent = Double(balance.amountMinutes - balance.remainingMinutes) / Double(balance.amountMinutes) * 100
        let smsUsagePercent = Double(balance.amountSms - balance.remainingSms) / Double(balance.amountSms) * 100
        
        // If user uses a lot of data
        if dataUsagePercent > 70 {
            let dataPackages = ["Süper İnternet", "Full Paket", "Sosyal Medya Paketi"]
            recommendations.append(contentsOf: packages.filter { dataPackages.contains($0.packageName) })
        }
        
        // If user uses a lot of minutes
        if minutesUsagePercent > 70 {
            let callPackages = ["Mega Konuşma", "Aile Paketi", "Full Paket"]
            recommendations.append(contentsOf: packages.filter { callPackages.contains($0.packageName) })
        }
        
        // Remove duplicates and limit to 3
        let uniqueRecommendations = Array(Set(recommendations))
        return Array(uniqueRecommendations.prefix(3))
    }
    
    private func getAIRecommendations(balance: BalanceResponse) async throws -> [Package] {
        // Create usage summary for AI
        let usagePrompt = """
        Kullanıcının mevcut paketi: \(balance.packageName)
        Kalan kaynak: \(balance.remainingData)MB internet, \(balance.remainingMinutes) dakika, \(balance.remainingSms) SMS
        Toplam kaynaklar: \(balance.amountData)MB internet, \(balance.amountMinutes) dakika, \(balance.amountSms) SMS
        
        Paketlerin Bilgisi:
        1. **Mini Öğrenci** (50 mins, 50 SMS, 1 GB, 25 TL)
        2. **Mini Konuşma** (100 mins, 50 SMS, 250 GB, 30 TL)
        3. **Mini İnternet** (3 GB, 50 mins, 30 SMS, 40 TL)
        4. **Genç Tarife** (4 GB, 200 mins, 100 SMS, 55 TL)
        5. **Sosyal Medya Paketi** (5 GB, 100 mins, 100 SMS, 60 TL)
        6. **Süper İnternet** (20 GB, 100 mins, 100 SMS, 80 TL)
        7. **Full Paket** (10 GB, 1000 mins, 500 SMS, 100 TL)
        8. **Aile Paketi** (8 GB, 1500 mins, 400 SMS, 120 TL)
        9. **Mega Konuşma** (1000 mins, 250 SMS, 1 GB, 75 TL)
        10. **Standart Konuşma** (250 mins, 100 SMS, 500 GB, 50 TL)
        
        Bu kullanım desenine göre hangi paketleri önerirsin? Sadece paket isimlerini virgülle ayırarak belirt.
        """
        
        let aiResponse = try await GeminiService.shared.sendMessage(
            prompt: usagePrompt,
            firstName: firstName,
            sms: balance.remainingSms,
            minutes: balance.remainingMinutes,
            data: balance.remainingData
        )
        
        // Parse AI response to extract package names
        let recommendedNames = parsePackageNamesFromAI(response: aiResponse)
        
        // Match with actual packages
        return packages.filter { package in
            recommendedNames.contains { recommendedName in
                package.packageName.localizedCaseInsensitiveContains(recommendedName) ||
                recommendedName.localizedCaseInsensitiveContains(package.packageName)
            }
        }.prefix(3).map { $0 }
    }
    
    private func parsePackageNamesFromAI(response: String) -> [String] {
        // Extract package names from AI response
        let packageNames = [
            "Mini Öğrenci", "Mini Konuşma", "Mini İnternet", "Genç Tarife",
            "Sosyal Medya Paketi", "Süper İnternet", "Full Paket", "Aile Paketi",
            "Mega Konuşma", "Standart Konuşma"
        ]
        
        return packageNames.filter { packageName in
            response.localizedCaseInsensitiveContains(packageName)
        }
    }
    
}
struct PackageCardView: View {
    let package: Package
    let isSelected: Bool
    let onSelect: () -> Void
    @Binding var forceRefresh: Bool
    let hasActivePackage: Bool
    var isRecommended: Bool = false

    @AppStorage("customerId") private var customerId: Int = 0
    @AppStorage("msisdn") private var msisdn: String = ""
    
    @State private var isActivating = false
    @State private var activationMessage: String?
    @State private var showConfirmationDialog = false
    @State private var showActivationAlert = false
    @State private var selectedPackageToActivate: Package?

    var body: some View {
        Button(action: onSelect) {
            content
        }
        .buttonStyle(PlainButtonStyle())
        // First: Confirmation alert before activation
        .alert("Paket Aktivasyonu", isPresented: $showActivationAlert) {
            Button("Tamam") {
                if let pkg = selectedPackageToActivate {
                    activatePackage(pkg)
                }
            }
            Button("İptal", role: .cancel) {
                selectedPackageToActivate = nil
            }
        } message: {
            Text("Seçtiğiniz paketi etkinleştirmek istiyor musunuz?")
        }

        // Second: Success or error alert *after* activation
        .alert("Bilgilendirme", isPresented: Binding<Bool>(
            get: { activationMessage != nil },
            set: { newValue in
                if !newValue {
                    // Just in case activationMessage was already nil (double dismissal)
                    activationMessage = nil
                }
            }
        )) {
            Button("Tamam") {
                let delay = 0.3
                if activationMessage != nil {
                    activationMessage = nil
                    DispatchQueue.main.async {
                                forceRefresh.toggle()
                            }
                }
            }
        } message: {
            Text(activationMessage ?? "İşlem sonucu görüntülenemedi.") // fallback
        }
    }

    private var content: some View {
        VStack(alignment: .leading, spacing: 10) {
            header

            if isSelected {
                details
                activateButton
            }
        }
        .padding()
        .background(hasActivePackage ? Color.gray.opacity(0.05) : Color.white)
        .cornerRadius(15)
        .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
        .overlay(
            RoundedRectangle(cornerRadius: 15)
                .stroke(borderColor, lineWidth: isRecommended ? 2 : 1)
        )
    }

    private var header: some View {
        HStack {
            VStack(alignment: .leading) {
                HStack {
                    Text(package.packageName)
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundColor(hasActivePackage ? .gray : .black)
                    if isRecommended {
                        Image(systemName: "star.fill")
                            .foregroundColor(.orange)
                            .font(.caption)
                    }
                }

                if !isSelected {
                    Text("\(package.amountMinutes) dk \(package.amountSms) sms \(package.amountData / 1000) GB")
                        .font(.subheadline)
                        .foregroundColor(Color.blue.opacity(0.7))
                }
            }

            Spacer()

            Image(systemName: "arrow.forward")
                .font(.title2)
                .foregroundColor(arrowColor)
                .rotationEffect(.degrees(isSelected ? 90 : 0))
        }
    }

    private var details: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text("\(package.amountMinutes) Dakika")
            Text("\(package.amountSms) SMS")
            Text("\(package.amountData / 1000) GB")
            Text("Süre: \(package.period) Gün")
            Text(String(format: "%.2f TL", package.price))
        }
        .font(.subheadline)
        .foregroundColor(hasActivePackage ? .gray : .black)
    }

    private var activateButton: some View {
        Button(action: {
            showConfirmationDialog = true
        }) {
            Group {
                if isActivating {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                        .padding()
                } else {
                    Text("Tarife Seç")
                        .font(.headline)
                        .foregroundColor(.white)
                        .padding()
                        .frame(maxWidth: .infinity)
                        .background(isRecommended ? Color.orange.opacity(0.8) : Color.purple.opacity(0.6))
                        .cornerRadius(10)
                }
            }
        }
        .disabled(isActivating)
        .confirmationDialog(
            "Bu paketi etkinleştirmek istediğinize emin misiniz?",
            isPresented: $showConfirmationDialog,
            titleVisibility: .visible
        ) {
            Button("Evet, Etkinleştir", role: .destructive) {
                selectedPackageToActivate = package
                showActivationAlert = true
            }
            Button("İptal", role: .cancel) {}
        }
    }

    private var borderColor: Color {
        if hasActivePackage {
            return Color.gray.opacity(0.3)
        } else if isRecommended {
            return Color.orange.opacity(0.6)
        } else if isSelected {
            return Color.purple.opacity(0.6)
        } else {
            return Color.gray.opacity(0.2)
        }
    }

    private var arrowColor: Color {
        if hasActivePackage {
            return Color.gray.opacity(0.5)
        } else if isRecommended {
            return Color.orange.opacity(0.6)
        } else {
            return Color.purple.opacity(0.6)
        }
    }

    private func activatePackage(_ pkg: Package) {
        Task {
            isActivating = true
            defer { isActivating = false }

            do {
                print("📦 Activating package: \(pkg.packageName) for customer: \(customerId), MSISDN: \(msisdn)")

                let newBalance = try await PackageService.selectPackage(
                    customerId: customerId,
                    packageId: pkg.package_id,
                    msisdn: msisdn
                )

                let successMessage = """
                ✅ \(newBalance.packageName) başarıyla etkinleştirildi!

                📅 Geçerlilik tarihi: \(formatDate(newBalance.edate))
                💰 Fiyat: \(String(format: "%.2f", newBalance.price)) TL

                Yeni Bakiyeniz:
                📱 \(Int(newBalance.remainingData / 1000)) GB İnternet
                📞 \(newBalance.remainingMinutes) Dakika  
                💬 \(newBalance.remainingSms) SMS
                """

                activationMessage = successMessage
                //forceRefresh.toggle()

                print("✅ Paket etkinleştirildi: \(newBalance.packageName)")
            } catch {
                activationMessage = "❌ Paket etkinleştirilemedi: \(error.localizedDescription)"
                print("❌ Paket aktivasyonu hatası: \(error)")
            }
        }
    }

    private func formatDate(_ raw: String) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        if let date = formatter.date(from: raw) {
            let output = DateFormatter()
            output.dateFormat = "dd MMM yyyy HH:mm"
            return output.string(from: date)
        }
        return raw
    }
}
