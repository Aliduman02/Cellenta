//
//  HomeView.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 17.07.2025.
//

import SwiftUI

enum Tab {
    case home, store, bills, profile, chatbot
}

struct HomeView: View {
    @State var selectedTab: Tab = .home
    @State private var showChatbot = false
    @State private var forceRefresh = false
    @AppStorage("name") var name: String = ""
    @AppStorage("msisdn") var msisdn: String = ""

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                switch selectedTab {
                case .home:
                    HomeContentView(forceRefresh: $forceRefresh)
                case .store:
                    StoreView(selectedTab: $selectedTab, forceRefresh: $forceRefresh)
                case .bills:
                    BillsView(selectedTab: $selectedTab)
                case .profile:
                    ProfileView(selectedTab: $selectedTab)
                case .chatbot:
                    ChatbotView(selectedTab: $selectedTab)
                }

                CustomTabBar(selectedTab: $selectedTab)
            }
            .navigationBarHidden(true)
        }
    }
}

// MARK: - Main Home Content
struct HomeContentView: View {
    @ObservedObject var session = UserSession.shared
    @Binding var forceRefresh: Bool

    @State private var balance: BalanceResponse? = nil
    @State private var hasLoaded = false

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // User Greeting
                HStack {
                    VStack(alignment: .leading) {
                        Text("Merhaba, \(session.name)")//Hello,
                            .font(.title2)
                            .fontWeight(.medium)
                            .foregroundColor(.gray)
                        Text(session.msisdn)
                            .font(.title3)
                            .fontWeight(.bold)
                            .foregroundColor(.black)
                    }
                    Spacer()
                }
                .padding(.horizontal)
                .padding(.top, 10)

                // Balance View
                if let balance = balance {
                    TariffCardView(packageName: balance.packageName, price: balance.price)
                        .padding(.horizontal)

                    HStack {
                        Text("Tarife Bilgisi")//Tariff Information
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(Color(red: 0.2, green: 0.6, blue: 0.7))
                        Spacer()
                    }
                    .padding(.horizontal)
                    .padding(.top, 10)

                    VStack(spacing: 20) {
                        HStack(spacing: 20) {
                            UsageCircleView(
                                currentValue: Double(balance.remainingMinutes),
                                maxValue: Double(balance.amountMinutes),
                                unit: "Kalan dakika",//Min left
                                totalUnit: "DK"//MIN
                            )
                            UsageCircleView(
                                currentValue: Double(balance.remainingData) / 1000.0,
                                maxValue: Double(balance.amountData) / 1000.0,
                                unit: "Kalan GB",
                                totalUnit: "GB"
                            )
                        }
                        UsageCircleView(
                            currentValue: Double(balance.remainingSms),
                            maxValue: Double(balance.amountSms),
                            unit: "Kalan SMS",//SMS left
                            totalUnit: "SMS"
                        )
                        .padding(.horizontal, 80)
                    }
                    .padding(.horizontal)
                } else {
                    ProgressView("Yükleniyor...")//Loading...
                }

                Spacer().frame(height: 100)
            }
        }
        .background(Color.white.edgesIgnoringSafeArea(.all))
        .onAppear {
            if !hasLoaded {
                print("🟡 First load triggered")
                Task { await loadBalance() }
                hasLoaded = true
            }
        }
        .onChange(of: forceRefresh) { _, newValue in
            if newValue {
                print("🔁 forceRefresh changed: \(newValue)")
                Task { await loadBalance() }
            }
        }
    }

    // MARK: - Load Balance
    func loadBalance() async {
        print("🔄 loadBalance() triggered")

        do {
            let newBalance = try await BalanceService.fetchBalance(msisdn: session.msisdn)
            self.balance = newBalance
            print("✅ New balance received: \(String(describing: newBalance))")
        } catch {
            print("❌ Failed to load balance: \(error)")
            self.balance = BalanceResponse(
                remainingMinutes: 0,
                remainingData: 0,
                remainingSms: 0,
                sdate: "",
                edate: "",
                packageName: "No Package",
                price: 0.0,
                amountMinutes: 0,
                amountData: 0,
                amountSms: 0,
                period: 0
            )
        }

        // Reset refresh trigger
        forceRefresh = false
    }
}

// MARK: - Custom Tab Bar
struct CustomTabBar: View {
    @Binding var selectedTab: Tab

    var body: some View {
        HStack {
            Spacer()

            Button(action: {
                selectedTab = .home
            }) {
                TabBarButton(imageName: "house.fill", title: "Ana Sayfa", isSelected: selectedTab == .home)
            }

            Spacer()

            Button(action: {
                selectedTab = .store
            }) {
                TabBarButton(imageName: "bag.fill", title: "Mağaza", isSelected: selectedTab == .store)
            }

            Spacer()

            Button(action: {
                selectedTab = .chatbot
            }) {
                VStack(spacing: 4) {
                    Image(selectedTab == .chatbot ? "CellentaButton" : "CellentaButton2")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 60, height: 60)
                    Text("Nasıl yardımcı olabilirim?")//"How can I help?"
                        .font(.caption)
                        .foregroundColor(selectedTab == .chatbot ? .teal : .gray)
                }
            }
            .offset(y: -11)

            Spacer()

            Button(action: {
                selectedTab = .bills
            }) {
                TabBarButton(imageName: "bolt.fill", title: "Faturalar", isSelected: selectedTab == .bills)//Bills
            }

            Spacer()

            Button(action: {
                selectedTab = .profile
            }) {
                TabBarButton(imageName: "person.fill", title: "Hesabım", isSelected: selectedTab == .profile)//Profile
            }

            Spacer()
        }
        .padding(.vertical, 10)
        .background(Color.white)
        .cornerRadius(20)
        .shadow(radius: 10)
        .padding(.horizontal)
    }
}

// MARK: - TabBarButton
struct TabBarButton: View {
    let imageName: String
    let title: String
    let isSelected: Bool

    var body: some View {
        VStack {
            Image(systemName: imageName)
                .font(.title2)
                .foregroundColor(isSelected ? Color(red: 0.2, green: 0.6, blue: 0.7) : .gray)
            Text(title)
                .font(.caption2)
                .foregroundColor(isSelected ? Color(red: 0.2, green: 0.6, blue: 0.7) : .gray)
        }
    }
}

// MARK: - TariffCardView
struct TariffCardView: View {
    let packageName: String
    let price: Double

    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text("Aktif Tarife")//Active Package
                    .font(.caption)
                    .foregroundColor(.white.opacity(0.8))
                Text(packageName)
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
            }
            Spacer()
            VStack(alignment: .trailing) {
                Text("\(Int(price)) TL")
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                Text("/ay")//month
                    .font(.caption)
                    .foregroundColor(.white.opacity(0.8))
            }
        }
        .padding()
        .background(
            LinearGradient(
                gradient: Gradient(colors: [Color.purple.opacity(0.6), Color.purple.opacity(0.8)]),
                startPoint: .leading,
                endPoint: .trailing
            )
        )
        .cornerRadius(15)
    }
}

// MARK: - UsageCircleView
struct UsageCircleView: View {
    let currentValue: Double
    let maxValue: Double
    let unit: String
    let totalUnit: String

    var progress: Double {
        maxValue > 0 ? currentValue / maxValue : 0
    }

    var body: some View {
        VStack {
            ZStack {
                Circle()
                    .stroke(lineWidth: 10)
                    .opacity(0.1)
                    .foregroundColor(Color.gray)

                Circle()
                    .trim(from: 0.0, to: CGFloat(min(progress, 1.0)))
                    .stroke(style: StrokeStyle(lineWidth: 10, lineCap: .round, lineJoin: .round))
                    .fill(
                        LinearGradient(
                            gradient: Gradient(colors: [Color(red: 0.2, green: 0.8, blue: 0.7), Color(red: 0.2, green: 0.6, blue: 0.7)]),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .rotationEffect(Angle(degrees: 270.0))
                    .animation(.easeOut(duration: 0.5), value: progress)

                VStack {
                    Text(String(format: "%g", currentValue))
                        .font(.title)
                        .fontWeight(.bold)
                        .foregroundColor(.black)
                    Text(unit)
                        .font(.caption)
                        .foregroundColor(.gray)
                }
            }
            .frame(width: 120, height: 120)

            Text("\(Int(maxValue)) \(totalUnit)")
                .font(.subheadline)
                .foregroundColor(.gray)
        }
    }
}

// MARK: - Previews
#Preview {
    HomeView()
}




