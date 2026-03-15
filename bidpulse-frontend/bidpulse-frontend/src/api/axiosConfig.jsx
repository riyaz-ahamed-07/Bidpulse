import axios from 'axios';

const apiClient = axios.create({
    baseURL: `${import.meta.env.VITE_API_BASE_URL}/api`,
    headers: { 'Content-Type': 'application/json' },
});

// Request Interceptor: Attach the Access Token to every outgoing request
apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) config.headers['Authorization'] = `Bearer ${token}`;
        return config;
    },
    (error) => Promise.reject(error)
);

// Response Interceptor: Catch 401s and silently refresh the token
apiClient.interceptors.response.use(
    (response) => response, // If the request succeeds, just return the data
    async (error) => {
        const originalRequest = error.config;

        // If it's a 401 Unauthorized, and we haven't tried refreshing yet...
        // Now it catches BOTH 401 (Unauthorized) and 403 (Forbidden) to attempt a refresh!
        if ((error.response?.status === 401 || error.response?.status === 403) && !originalRequest._retry) {
            originalRequest._retry = true; // Mark this request so we don't get stuck in an infinite loop

            try {
                const refreshToken = localStorage.getItem('refreshToken');
                if (!refreshToken) throw new Error("No refresh token available");

                // Ask Spring Boot for a new token pair
                // (Make sure this matches your actual backend refresh endpoint!)
                const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/api/auth/refresh`, {
                    refreshToken: refreshToken
                });

                // Save the shiny new tokens
                localStorage.setItem('accessToken', response.data.accessToken);
                if (response.data.refreshToken) {
                    localStorage.setItem('refreshToken', response.data.refreshToken);
                }

                // Update the original failed request with the new token and try again!
                originalRequest.headers['Authorization'] = `Bearer ${response.data.accessToken}`;
                return apiClient(originalRequest);

            } catch (refreshError) {
                // If the refresh token is ALSO expired, nuke everything and force log out
                console.error("Session expired. Logging out.", refreshError);
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                window.location.href = '/login'; // Kick them to the curb
                return Promise.reject(refreshError);
            }
        }
        return Promise.reject(error);
    }
);

export default apiClient;