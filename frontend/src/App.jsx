
import { useState } from "react";
import Login from "./pages/Login";
import Dashboard from "./Dashboard";

function App() {
    const [session, setSession] = useState(() => {
        const token = localStorage.getItem("token");
        const role = localStorage.getItem("role");

        return token && role ? { token, role } : null;
    });
    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        setSession(null);
    };

    if (!session) {
        return <Login onLogin={setSession} />;
    }

    return (
        <Dashboard
            role={session.role}
            onLogout={handleLogout}
        />
    );
}

export default App;
