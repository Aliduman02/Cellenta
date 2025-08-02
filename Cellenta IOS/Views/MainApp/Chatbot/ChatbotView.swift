//
//  ChatbotView.swift
//  Cellenta IOS
//
//  Created by Atena Jafari Parsa on 18.07.2025.
//
import SwiftUI

struct ChatbotView: View {
    @ObservedObject var session = UserSession.shared
    @State private var smsBalance: Int = 0
    @State private var minuteBalance: Int = 0
    @State private var dataBalance: Int = 0
    @State private var hasLoadedInitialMessage = false
    @State private var messages: [Message] = []
    @Binding var selectedTab: Tab

    @State private var inputText: String = ""

    var body: some View {
        VStack(spacing: 0) {
            // MARK: - Custom Navigation Bar
            HStack {
                Image(systemName: "chevron.left")
                    .padding(.trailing, 4)
                Text("Chatbot")
                    .font(.headline)
                Spacer()
            }
            .padding()
            .background(Color.teal)
            .foregroundColor(.white)

            // MARK: - Messages
            ScrollView {
                VStack(spacing: 16) {
                    ForEach(messages) { message in
                        HStack(alignment: .bottom, spacing: 8) {
                            if message.sender == .bot {
                                Image("Cellentaa")
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                                    .frame(width: 36, height: 36)
                                    .clipShape(Circle())
                                VStack(alignment: .leading) {
                                    Text("Cellenta Bot")
                                        .font(.caption)
                                        .foregroundColor(.gray)
                                    Text(message.text)
                                        .padding()
                                        .background(Color.gray.opacity(0.1))
                                        .cornerRadius(12)
                                        .foregroundColor(.black)
                                }
                                Spacer()
                            } else {
                                Spacer()
                                VStack(alignment: .trailing) {
                                    Text("Kullanıcı")//You
                                        .font(.caption)
                                        .foregroundColor(.gray)
                                    Text(message.text)
                                        .padding()
                                        .background(Color.teal.opacity(0.2))
                                        .cornerRadius(12)
                                        .foregroundColor(.black)
                                }
                                Circle()
                                    .fill(Color.teal)
                                    .frame(width: 36, height: 36)
                                    .overlay(Image(systemName: "person.fill").foregroundColor(.white))
                            }
                        }
                        .padding(.horizontal)
                    }
                }
                .padding(.top)
            }

            // MARK: - Input Bar
            HStack {
                TextField("Bir mesaj gönder...", text: $inputText)//"Send a message"
                    .padding(10)
                    .background(Color(.systemGray6))
                    .cornerRadius(20)
                    .padding(.horizontal)

                Button(action: sendMessage) {
                    Image(systemName: "paperplane.fill")
                        .foregroundColor(.teal)
                        .padding()
                }
            }
            .padding(.bottom)
            .background(Color.white)
        }
        .ignoresSafeArea(edges: .bottom)
        .task {
            if !hasLoadedInitialMessage {
                await loadInitialMessage()
                hasLoadedInitialMessage = true
            }
        }
    }

    // MARK: - Send Message to Gemini
    func sendMessage() {
        guard !inputText.trimmingCharacters(in: .whitespaces).isEmpty else { return }

        let userMessage = inputText
        messages.append(Message(id: UUID(), text: userMessage, sender: .user))
        inputText = ""

        Task {
            do {
                let firstName = session.name.components(separatedBy: " ").first
                let botReply = try await GeminiService.shared.sendMessage(
                    prompt: userMessage,
                    firstName: firstName,
                    sms: smsBalance,
                    minutes: minuteBalance,
                    data: dataBalance
                )
                await MainActor.run {
                    messages.append(Message(id: UUID(), text: botReply, sender: .bot))
                }
            } catch {
                await MainActor.run {
                    messages.append(Message(id: UUID(), text: "Gemini’den yanıt alınamadı: \(error.localizedDescription)", sender: .bot))//"Failed to get response from Gemini:
                }
            }
        }
    }

    // MARK: - Load Initial Bot Message
    func loadInitialMessage() async {
        do {
            let balance = try await BalanceService.fetchBalance(msisdn: session.msisdn)
            smsBalance = balance.remainingSms
            minuteBalance = balance.remainingMinutes
            dataBalance = balance.remainingData

            let firstName = session.name.components(separatedBy: " ").first ?? session.name
            /*let welcomeMessage = """
            Hi \(firstName)! Welcome to Cellenta. You have:
            - \(smsBalance) SMS
            - \(minuteBalance) minutes
            - \(dataBalance) GB of data
            remaining in your balance.
            How can I help you today?
            """*/
            let welcomeMessage = """
            Merhaba \(firstName)! Cellenta’ya hoş geldiniz. Bakiyenizde:
            - \(smsBalance) SMS
            - \(minuteBalance) dakika
            - \(String(format: "%.3f", Double(dataBalance) / 1024.0)) GB internet
            kaldı.
            Size bugün nasıl yardımcı olabilirim?
            """

            await MainActor.run {
                messages = [Message(id: UUID(), text: welcomeMessage, sender: .bot)]
            }
        } catch {
            let fallbackMessage = "Merhaba \(session.name)! Cellenta’ya hoş geldin. Bakiye bilgilerin yüklenemedi, ancak sana yardımcı olmak için buradayım!"
            //"Hi \(session.name)! Welcome to Cellenta. I couldn’t load your balance info, but I’m here to help!"
            await MainActor.run {
                messages = [Message(id: UUID(), text: fallbackMessage, sender: .bot)]
            }
        }
    }
}

// MARK: - Message Model
struct Message: Identifiable {
    let id: UUID
    let text: String
    let sender: Sender
}

enum Sender {
    case user
    case bot
}

// MARK: - Preview
#Preview {
    ChatbotView(selectedTab: .constant(.chatbot))
}
