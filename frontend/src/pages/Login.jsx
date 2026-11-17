import { useState } from "react";
import { ShieldCheck, ArrowRight, Eye, EyeOff, LockKeyhole } from "lucide-react";
import api from "../api";

function Login({ onLogin }) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleLogin = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            const response = await api.post("/api/users/login", {
                username,
                password,
            });

            const data = response.data;
            const token = data.token || data.jwt;
            const role = data.role;

            if (!token || !role) {
                setError("Unable to verify your login response.");
                return;
            }

            localStorage.setItem("token", token);
            localStorage.setItem("role", role);
            localStorage.setItem("userId", data.userId);
            localStorage.setItem("username", data.username);


            onLogin({ token, role });
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Invalid credentials. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-[#080e1a] text-white lg:grid lg:grid-cols-2">
            {/* Left branding panel */}
            <section className="relative hidden min-h-screen overflow-hidden border-r border-white/[0.06] lg:flex lg:flex-col lg:justify-between lg:p-12 xl:p-16">
                <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_15%_25%,rgba(8,145,178,0.13),transparent_48%)]" />

                <div className="relative z-10 flex items-center gap-3">
                    <div className="flex h-10 w-10 items-center justify-center rounded-xl border border-cyan-400/20 bg-cyan-400/10">
                        <ShieldCheck size={23} className="text-cyan-400" />
                    </div>
                    <div>
                        <p className="text-xl font-semibold tracking-tight">VaultPay</p>
                        <p className="text-[10px] uppercase tracking-[0.25em] text-slate-500">
                            Digital Banking
                        </p>
                    </div>
                </div>

                <div className="relative z-10 max-w-xl">
                    <p className="mb-5 text-xs font-semibold uppercase tracking-[0.3em] text-cyan-400">
                        Banking, redefined
                    </p>

                    <h1 className="text-5xl font-semibold leading-[1.16] tracking-tight xl:text-6xl">
                        Your money.
                        <br />
                        Your control.
                        <br />
                        <span className="text-slate-400">Your future.</span>
                    </h1>

                    <p className="mt-6 max-w-md text-base leading-7 text-slate-400">
                        Manage your accounts, track transactions, and move money with a
                        banking experience designed around clarity and control.
                    </p>

                    <div className="mt-10 flex items-center gap-3 border-t border-white/10 pt-6">
                        <LockKeyhole size={17} className="text-cyan-400" />
                        <p className="text-sm text-slate-400">
                            Secure access to your banking dashboard
                        </p>
                    </div>
                </div>

                <p className="relative z-10 text-xs text-slate-600">
                    © {new Date().getFullYear()} VaultPay. All rights reserved.
                </p>
            </section>

            {/* Login panel */}
            <section className="flex min-h-screen items-center justify-center px-6 py-12 sm:px-10">
                <div className="w-full max-w-md">
                    <div className="mb-12 flex items-center gap-3 lg:hidden">
                        <div className="flex h-10 w-10 items-center justify-center rounded-xl border border-cyan-400/20 bg-cyan-400/10">
                            <ShieldCheck size={23} className="text-cyan-400" />
                        </div>
                        <span className="text-xl font-semibold tracking-tight">
                            VaultPay
                        </span>
                    </div>

                    <div className="mb-9">
                        <p className="mb-3 text-xs font-semibold uppercase tracking-[0.24em] text-cyan-400">
                            Welcome back
                        </p>
                        <h2 className="text-3xl font-semibold tracking-tight sm:text-4xl">
                            Sign in to VaultPay
                        </h2>
                        <p className="mt-3 text-sm leading-6 text-slate-400">
                            Enter your credentials to access your account.
                        </p>
                    </div>

                    <form onSubmit={handleLogin} className="space-y-6">
                        <div>
                            <label
                                htmlFor="username"
                                className="mb-2 block text-sm font-medium text-slate-300"
                            >
                                Username
                            </label>
                            <input
                                id="username"
                                type="text"
                                autoComplete="username"
                                placeholder="Enter your username"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required
                                className="h-12 w-full rounded-lg border border-slate-700/80 bg-[#0d1524] px-4 text-sm text-white outline-none transition placeholder:text-slate-600 focus:border-cyan-500 focus:ring-2 focus:ring-cyan-500/10"
                            />
                        </div>

                        <div>
                            <div className="mb-2 flex items-center justify-between">
                                <label
                                    htmlFor="password"
                                    className="block text-sm font-medium text-slate-300"
                                >
                                    Password
                                </label>
                            </div>

                            <div className="relative">
                                <input
                                    id="password"
                                    type={showPassword ? "text" : "password"}
                                    autoComplete="current-password"
                                    placeholder="Enter your password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                    className="h-12 w-full rounded-lg border border-slate-700/80 bg-[#0d1524] px-4 pr-12 text-sm text-white outline-none transition placeholder:text-slate-600 focus:border-cyan-500 focus:ring-2 focus:ring-cyan-500/10"
                                />

                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    aria-label={showPassword ? "Hide password" : "Show password"}
                                    className="absolute inset-y-0 right-0 flex w-12 items-center justify-center text-slate-500 transition hover:text-white"
                                >
                                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                                </button>
                            </div>
                        </div>

                        {error && (
                            <div
                                role="alert"
                                className="rounded-lg border border-red-500/20 bg-red-500/[0.07] px-4 py-3 text-sm text-red-300"
                            >
                                {error}
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={loading}
                            className="group flex h-12 w-full items-center justify-center gap-2 rounded-lg bg-cyan-500 px-4 text-sm font-semibold text-[#06111b] transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            {loading ? "Signing in..." : "Sign in"}
                            {!loading && (
                                <ArrowRight
                                    size={17}
                                    className="transition-transform group-hover:translate-x-1"
                                />
                            )}
                        </button>
                    </form>

                    <div className="mt-8 flex items-start gap-3 border-t border-white/[0.07] pt-6">
                        <LockKeyhole size={16} className="mt-0.5 shrink-0 text-slate-500" />
                        <p className="text-xs leading-5 text-slate-500">
                            Your session is protected by VaultPay authentication and
                            role-based access controls.
                        </p>
                    </div>

                    <p className="mt-10 text-center text-xs text-slate-600 lg:text-left">
                        © {new Date().getFullYear()} VaultPay
                    </p>
                </div>
            </section>
        </div>
    );
}

export default Login;
