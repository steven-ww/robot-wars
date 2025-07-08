///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.7.5
//DEPS org.zeroturnaround:zt-exec:1.12
//JAVA 21
//JAVA_OPTIONS --enable-preview
//RUNTIME_OPTIONS --enable-preview
//GAV org.nodejs:node:18.18.2

import picocli.CommandLine;
import picocli.CommandLine.Command;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Command(name = "start-dev", mixinStandardHelpOptions = true, version = "start-dev 1.0",
        description = "Starts the Robot Wars development environment")
public class start_dev implements Callable<Integer> {

    private static final String BACKEND_URL = "http://localhost:8080";
    private static final String FRONTEND_URL = "http://localhost:3000";
    private static final int BROWSER_LAUNCH_DELAY_SECONDS = 5;
    private static final int BACKEND_CHECK_INTERVAL_MS = 500;
    private static final int BACKEND_TIMEOUT_SECONDS = 120; // 2 minutes timeout

    public static void main(String... args) {
        int exitCode = new CommandLine(new start_dev()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Checks if the backend server is up and running.
     * 
     * @return true if the backend is ready, false otherwise
     */
    private boolean isBackendReady() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(BACKEND_URL).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            int responseCode = connection.getResponseCode();
            return responseCode >= 200 && responseCode < 500; // Any non-server error response means server is up
        } catch (IOException e) {
            return false; // Connection failed, backend not ready
        }
    }

    /**
     * Waits for the backend to be ready, with a timeout.
     * 
     * @return true if backend became ready within the timeout, false if it timed out
     */
    private boolean waitForBackendReady() throws InterruptedException {
        System.out.println("Waiting for backend server to be ready...");
        long startTime = System.currentTimeMillis();
        long timeoutMillis = TimeUnit.SECONDS.toMillis(BACKEND_TIMEOUT_SECONDS);

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (isBackendReady()) {
                System.out.println("Backend server is ready!");
                return true;
            }
            Thread.sleep(BACKEND_CHECK_INTERVAL_MS);
        }

        System.err.println("Timed out waiting for backend server to start");
        return false;
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Starting Robot Wars development environment...");

        // Start backend
        CompletableFuture<Void> backendFuture = CompletableFuture.runAsync(() -> {
            try {
                System.out.println("Starting backend server (Quarkus dev mode)...");
                String gradleCommand = System.getProperty("os.name").toLowerCase().contains("windows") ? "gradlew.bat" : "./gradlew";

                new ProcessExecutor()
                    .command(gradleCommand, "quarkusDev")
                    .redirectOutput(new LogOutputStream() {
                        @Override
                        protected void processLine(String line) {
                            System.out.println("[BACKEND] " + line);
                        }
                    })
                    .start();
            } catch (Exception e) {
                System.err.println("Error starting backend: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Wait for backend to be ready before starting frontend
        if (!waitForBackendReady()) {
            System.err.println("Backend server failed to start in time. Exiting.");
            return 1;
        }

        // Start frontend only after backend is ready
        CompletableFuture<Void> frontendFuture = CompletableFuture.runAsync(() -> {
            try {
                System.out.println("Starting frontend server...");

                new ProcessExecutor()
                    .command("npm", "start")
                    .redirectOutput(new LogOutputStream() {
                        @Override
                        protected void processLine(String line) {
                            System.out.println("[FRONTEND] " + line);
                        }
                    })
                    .directory(new File("frontend"))
                    .start();
            } catch (Exception e) {
                System.err.println("Error starting frontend: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Wait a bit for frontend to start before opening browser
        System.out.println("Waiting for frontend server to start...");
        Thread.sleep(TimeUnit.SECONDS.toMillis(BROWSER_LAUNCH_DELAY_SECONDS));

        // Open browser
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            System.out.println("Opening browser to " + FRONTEND_URL);
            Desktop.getDesktop().browse(new URI(FRONTEND_URL));
        } else {
            System.out.println("Could not open browser automatically. Please navigate to " + FRONTEND_URL);
        }

        System.out.println("\nDevelopment environment started!");
        System.out.println("- Frontend: " + FRONTEND_URL);
        System.out.println("- Backend: " + BACKEND_URL);
        System.out.println("\nPress Ctrl+C to stop all servers.");

        // Keep the main thread alive
        Thread.currentThread().join();

        return 0;
    }
}
