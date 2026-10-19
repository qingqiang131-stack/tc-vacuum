import SwiftUI
import WebKit

@main
struct ChanKhongApp: App {
    var body: some Scene {
        WindowGroup {
            StudyWebView()
                .preferredColorScheme(.light)
        }
    }
}

private struct StudyWebView: UIViewRepresentable {
    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView(frame: .zero)
        webView.navigationDelegate = context.coordinator
        webView.uiDelegate = context.coordinator
        webView.allowsBackForwardNavigationGestures = true
        webView.isOpaque = false
        webView.backgroundColor = .systemBackground
        webView.scrollView.contentInsetAdjustmentBehavior = .automatic

        if let indexURL = Bundle.main.url(forResource: "index", withExtension: "html"),
           let webContentURL = Bundle.main.resourceURL {
            webView.loadFileURL(indexURL, allowingReadAccessTo: webContentURL)
        }

        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}

    final class Coordinator: NSObject, WKNavigationDelegate, WKUIDelegate {
        func webView(
            _ webView: WKWebView,
            createWebViewWith configuration: WKWebViewConfiguration,
            for navigationAction: WKNavigationAction,
            windowFeatures: WKWindowFeatures
        ) -> WKWebView? {
            // Open links marked target="_blank" in the app's existing web view.
            guard navigationAction.targetFrame == nil else { return nil }
            webView.load(navigationAction.request)
            return nil
        }
    }
}
