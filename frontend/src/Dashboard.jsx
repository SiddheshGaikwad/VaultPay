import {
	LayoutDashboard,
	Wallet,
	ArrowLeftRight,
	History,
	Settings,
	Bell,
	Search,
	Sun,
	Moon,
	ArrowUpRight,
	ArrowDownLeft,
	CreditCard,
	ShieldCheck,
	Menu,
	X,
	Bot,
} from "lucide-react";
import { useEffect, useState } from "react";
import { getAccountsByUserId } from "./services/accountService";
import { getReceiverAccounts } from "./services/receiverAccountService";
import { getTransactionsByAccountId } from "./services/transactionService";
import { transferMoney } from "./services/transferService";
import { askAiAssistant } from "./services/aiAssistantService";


const navigation = [
	{ name: "Dashboard", icon: LayoutDashboard },
	{ name: "My Accounts", icon: Wallet },
	{ name: "Transfer Money", icon: ArrowLeftRight },
	{ name: "Transactions", icon: History },
	{ name: "AI Assistant", icon: Bot },
];

const transactions = [
	{ name: "Salary Credit", detail: "Today · 10:42 AM", amount: "+₹45,000.00", type: "credit" },
	{ name: "Amazon Purchase", detail: "Today · 09:18 AM", amount: "-₹2,499.00", type: "debit" },
	{ name: "Transfer Received", detail: "Yesterday · 04:36 PM", amount: "+₹3,500.00", type: "credit" },
	{ name: "Electricity Bill", detail: "Yesterday · 11:12 AM", amount: "-₹1,250.00", type: "debit" },
];

function Dashboard({ role, onLogout }) {

	const [dark, setDark] = useState(
		() => localStorage.getItem("vaultpay-theme") === "dark"
	);
	const [menuOpen, setMenuOpen] = useState(false);
	const [activePage, setActivePage] = useState("Dashboard");
	const [aiQuestion, setAiQuestion] = useState("");
	const [aiAnswer, setAiAnswer] = useState("");
	const [aiLoading, setAiLoading] = useState(false);
	const [aiError, setAiError] = useState("");
	const [transactionSearch, setTransactionSearch] = useState("");
	const [transactionFilter, setTransactionFilter] = useState("ALL");

	const theme = dark
		? "bg-[#080D19] text-slate-100"
		: "bg-[#F4F6FA] text-[#17243A]";

	const panel = dark
		? "bg-[#111827] border-white/[0.08]"
		: "bg-white border-[#E1E7F0] shadow-[0_8px_30px_rgba(28,45,75,0.045)]";

	const muted = dark
		? "text-slate-400"
		: "text-[#718096]";



	const [accounts, setAccounts] = useState([]);
	const [accountError, setAccountError] = useState("");

	const [receiverAccounts, setReceiverAccounts] = useState([]);
	const [receiverError, setReceiverError] = useState("");

	useEffect(() => {
		const userId = localStorage.getItem("userId");
		if (!userId) {
			setAccountError("User ID is missing. Please log in again.");
			return;
		}
		getAccountsByUserId(userId)
			.then(setAccounts)
			.catch(() => setAccountError("Unable to load accounts."));
	}, []);

	useEffect(() => {
		getReceiverAccounts()
			.then((data) => {
				setReceiverAccounts(data);
				setReceiverError("");
			})
			.catch((error) => {
				setReceiverError(
					error.response?.data?.message ||
					"Unable to load receiver accounts."
				);
			});
	}, []);

	const totalBalance = accounts.reduce(
		(total, account) => total + Number(account.balance || 0),
		0
	);

	const [recentTransactions, setRecentTransactions] = useState([]);
	const [allTransactions, setAllTransactions] = useState([]);
	const [transactionError, setTransactionError] = useState("");

	const [moneyReceived, setMoneyReceived] = useState(0);
	const [moneySent, setMoneySent] = useState(0);

	useEffect(() => {
		if (accounts.length === 0) return;

		const loadTransactions = async () => {
			try {
				const results = await Promise.all(
					accounts.map((account) =>
						getTransactionsByAccountId(account.id)
					)
				);

				const allTransactions = [
					...new Map(
						results
							.flat()
							.map((tx) => [tx.transactionId, tx])
					).values()
				];
				allTransactions.sort(
					(a, b) =>
						new Date(b.createdAt) - new Date(a.createdAt)
				);

				const successfulTransactions = allTransactions.filter(
					(tx) => tx.status === "SUCCESS"
				);

				const received = successfulTransactions
					.filter((tx) => tx.type === "DEPOSIT")
					.reduce((total, tx) => total + Number(tx.amount), 0);

				const sent = successfulTransactions
					.filter(
						(tx) =>
							tx.type === "TRANSFER" ||
							tx.type === "WITHDRAWAL"
					)
					.reduce((total, tx) => total + Number(tx.amount), 0);

				setMoneyReceived(received);
				setMoneySent(sent);
				setAllTransactions(allTransactions);
				setRecentTransactions(allTransactions.slice(0, 5));

				setTransactionError("");
			} catch (error) {
				setTransactionError("Unable to load transactions.");
			}
		};

		loadTransactions();
	}, [accounts]);

	const filteredTransactions = allTransactions.filter((tx) => {
		const search = transactionSearch.toLowerCase();

		const matchesSearch =
			String(tx.transactionId ?? "").toLowerCase().includes(search) ||
			String(tx.type ?? "").toLowerCase().includes(search) ||
			String(tx.status ?? "").toLowerCase().includes(search) ||
			String(tx.amount ?? "").includes(search);

		const matchesFilter =
			transactionFilter === "ALL" ||
			tx.status === transactionFilter;

		return matchesSearch && matchesFilter;
	});

	const [transferData, setTransferData] = useState({
		senderAccountId: "",
		receiverAccountNumber: "",
		amount: "",
	});

	const [transferMessage, setTransferMessage] = useState("");
	const [transferError, setTransferError] = useState("");
	const [transferLoading, setTransferLoading] = useState(false);

	const handleTransfer = async (e) => {
		e.preventDefault();
		setTransferMessage("");
		setTransferError("");

		const sender = accounts.find(
			(account) => String(account.id) === transferData.senderAccountId
		);

		if (!sender) {
			setTransferError("Please select a sender account.");
			return;
		}
		if (
			sender.accountNumber === transferData.receiverAccountNumber.trim()
		) {
			setTransferError("Sender and receiver accounts must be different.");
			return;
		}

		if (
			!transferData.receiverAccountNumber.trim() ||
			!Number.isFinite(Number(transferData.amount)) ||
			Number(transferData.amount) <= 0
		) {
			setTransferError(
				"Enter a valid receiver account number and amount."
			);
			return;
		}

		try {
			setTransferLoading(true);

			await transferMoney({
				senderAccountId: Number(transferData.senderAccountId),
				receiverAccountNumber:
					transferData.receiverAccountNumber.trim(),
				amount: Number(transferData.amount),
			});

			setTransferMessage("Transfer completed successfully!");
			setTransferData({
				senderAccountId: "",
				receiverAccountNumber: "",
				amount: "",
			});

			const refreshedAccounts = await getAccountsByUserId(
				localStorage.getItem("userId")
			);

			setAccounts(refreshedAccounts);

			setAccounts(refreshedAccounts);
		} catch (error) {
			setTransferError(
				error.response?.data?.message || "Transfer failed. Please try again."
			);
		} finally {
			setTransferLoading(false);
		}
	};

	const handleAskAi = async (e) => {
		e.preventDefault();

		if (!aiQuestion.trim() || aiLoading) {
			return;
		}

		setAiLoading(true);
		setAiError("");
		setAiAnswer("");

		try {
			const data = await askAiAssistant(aiQuestion.trim());
			setAiAnswer(data.answer);
		} catch (error) {
			setAiError(
				error.response?.data?.message ||
				error.response?.data?.error ||
				"Unable to connect to the AI assistant. Please try again."
			);
		} finally {
			setAiLoading(false);
		}
	};

	return (

		<div className={`min-h-screen ${theme} transition-colors duration-300`}>
			{menuOpen && (
				<button
					aria-label="Close menu"
					onClick={() => setMenuOpen(false)}
					className="fixed inset-0 z-30 bg-black/50 lg:hidden"
				/>
			)}


			<aside
				className={`fixed inset-y-0 left-0 z-40 flex w-64 flex-col overflow-y-auto sidebar-no-scrollbar border-r ${dark ? "border-white/10 bg-[#0D1422]" : "border-[#E5E9F1] bg-[#FCFDFE] shadow-[4px_0_24px_rgba(15,23,42,0.025)]"
					} p-5 transition-transform duration-300 lg:translate-x-0 ${menuOpen ? "translate-x-0" : "-translate-x-full"
					}`}
			>
				<div className="flex items-center justify-between">
					<div className="flex items-center gap-3">
						<div className="grid h-11 w-11 place-items-center rounded-2xl bg-gradient-to-br from-cyan-400 to-blue-600 text-xl font-black text-white shadow-lg shadow-cyan-500/20">
							V
						</div>
						<div>
							<h1 className="text-xl font-bold tracking-tight">VaultPay</h1>
							<p className={`text-[10px] tracking-[0.24em] ${muted}`}>
								DIGITAL BANKING
							</p>


						</div>
					</div>
					<button
						onClick={() => setMenuOpen(false)}
						className="rounded-lg p-2 lg:hidden"
						aria-label="Close sidebar"
					>
						<X size={20} />
					</button>



				</div>

				<p className={`mb-3 mt-10 px-3 text-[10px] font-bold tracking-[0.2em] ${muted}`}>
					WORKSPACE
				</p>

				<nav className="space-y-2">
					{navigation.map(({ name, icon: Icon }) => (
						<button
							key={name}
							onClick={() => {
								setActivePage(name);
								setMenuOpen(false);
							}}
							className={`flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm font-medium transition ${activePage === name
								? "bg-gradient-to-r from-cyan-500 to-blue-600 text-white shadow-lg shadow-blue-500/20"
								: dark
									? "text-slate-400 hover:bg-white/5 hover:text-white"
									: "text-slate-600 hover:bg-slate-100"
								}`}
						>
							<Icon size={19} />
							{name}
						</button>
					))}
				</nav>

				<div className="mt-auto">
					<div className={`mb-5 rounded-2xl border p-4 ${panel}`}>
						<div className="mb-3 flex items-center gap-2 text-cyan-500">
							<ShieldCheck size={20} />
							<span className="text-sm font-semibold">Security first</span>
						</div>
						<p className={`text-xs leading-5 ${muted}`}>
							Your banking activity is protected by secure authentication.
						</p>
					</div>

					<button
						onClick={() => setActivePage("Settings")}
						className={`flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm ${muted} hover:bg-slate-500/10`}
					>
						<Settings size={19} />
						Settings
					</button>

					<div className={`mt-4 flex items-center gap-3 border-t pt-4 ${dark ? "border-white/10" : "border-slate-200"
						}`}>
						<div className="grid h-10 w-10 place-items-center rounded-full bg-gradient-to-br from-indigo-400 to-cyan-500 font-bold text-white">
							U
						</div>
						<div className="min-w-0 flex-1">
							<p className="truncate text-sm font-semibold">{localStorage.getItem("username") || "User"}</p>
							<p className={`text-xs ${muted}`}>Customer account</p>
						</div>
					</div>
				</div>

				<button
					onClick={onLogout}
					className="mt-3 shrink-0 w-full rounded-lg px-4 py-3 text-left text-red-400 hover:bg-red-500/10"				>
					Logout
				</button>

			</aside>

			<main className="min-h-screen lg:ml-64">
				<header className={`sticky top-0 z-20 flex h-[76px] items-center justify-between border-b px-4 sm:px-8 ${dark ? "border-white/10 bg-[#080D19]/90" : "border-slate-200 bg-white/90"
					} backdrop-blur-xl`}>
					<div className="flex items-center gap-3">
						<button
							onClick={() => setMenuOpen(true)}
							className="rounded-xl p-2 hover:bg-slate-500/10 lg:hidden"
							aria-label="Open menu"
						>
							<Menu size={22} />
						</button>
						<div>
							<p className={`hidden text-xs sm:block ${muted}`}>Workspace / {activePage}</p>
							<h2 className="font-semibold">{activePage}</h2>
						</div>
					</div>

					<div className="flex items-center gap-2 sm:gap-4">
						<button
							aria-label="Search"
							className={`hidden rounded-xl border p-2.5 sm:block ${panel} ${muted}`}
						>
							<Search size={18} />
						</button>
						<button
							aria-label="Notifications"
							className={`relative rounded-xl border p-2.5 ${panel}`}
						>
							<Bell size={18} />
							<span className="absolute right-2 top-2 h-2 w-2 rounded-full bg-cyan-400" />
						</button>
						<button
							onClick={() => {
								const nextTheme = !dark;
								setDark(nextTheme);
								localStorage.setItem("vaultpay-theme", nextTheme ? "dark" : "light");
							}} aria-label={dark ? "Switch to light mode" : "Switch to dark mode"}
							className={`rounded-xl border p-2.5 transition hover:scale-105 ${panel}`}
							title={dark ? "Light mode" : "Dark mode"}
						>
							{dark ? <Sun size={18} className="text-amber-400" /> : <Moon size={18} />}
						</button>
						<button className="hidden rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white sm:block">
							Help center
						</button>
					</div>
				</header>

				<div className="mx-auto max-w-[1500px] space-y-8 p-5 sm:p-9">
					<section className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
						<div>
							<p className={`mb-2 text-sm ${muted}`}>Monday, your finances at a glance</p>
							<h1 className="text-2xl font-bold tracking-tight sm:text-3xl">
								Welcome back, {localStorage.getItem("username") || "User"}</h1>
							<p className={`mt-2 text-sm ${muted}`}>
								Here's what's happening with your money today.
							</p>
						</div>
						<button
							onClick={() => setActivePage("Transfer Money")}
							className="flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 px-5 py-3 text-sm font-semibold text-white shadow-lg shadow-blue-500/20 transition hover:-translate-y-0.5"
						>
							<ArrowLeftRight size={17} />
							Transfer money
							<ArrowUpRight size={16} />
						</button>
					</section>


					{activePage === "AI Assistant" && (
						<div className="mx-auto max-w-4xl space-y-6">
							<div className={`${panel} rounded-2xl p-6`}>
								<div className="mb-4 flex items-center gap-3">
									<div className="rounded-xl bg-blue-500/10 p-3">
										<Bot className="h-7 w-7 text-blue-500" />
									</div>

									<div>
										<h2 className="text-xl font-bold">VaultPay AI Assistant</h2>
										<p className={muted}>
											Ask questions about banking and financial concepts.
										</p>
									</div>
								</div>

								<form onSubmit={handleAskAi} className="space-y-4">
									<textarea
										value={aiQuestion}
										onChange={(e) => setAiQuestion(e.target.value)}
										placeholder="Example: What is the difference between debit and credit?"
										rows={4}
										maxLength={1000}
										className="w-full rounded-xl border border-slate-300 bg-transparent p-4 outline-none focus:border-blue-500 dark:border-slate-700"
									/>

									<button
										type="submit"
										disabled={aiLoading || !aiQuestion.trim()}
										className="rounded-xl bg-blue-600 px-6 py-3 font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
									>
										{aiLoading ? "Thinking..." : "Ask AI"}
									</button>
								</form>
							</div>

							{aiLoading && (
								<div className={`${panel} rounded-2xl p-5`}>
									<p className={muted}>Your assistant is preparing an answer...</p>
								</div>
							)}

							{aiError && (
								<div className="rounded-2xl border border-red-500/30 bg-red-500/10 p-5 text-red-500">
									{aiError}
								</div>
							)}

							{aiAnswer && (
								<div className={`${panel} rounded-2xl p-6`}>
									<h3 className="mb-3 text-lg font-bold">AI Response</h3>
									<p className="whitespace-pre-wrap leading-7">{aiAnswer}</p>
								</div>
							)}
						</div>
					)}


					{/* Transfer Money Form */}
					{activePage === "Transfer Money" && (
						<section className={`rounded-2xl border p-6 sm:p-8 ${panel}`}>
							<div className="mb-6">
								<h2 className="text-xl font-bold">Transfer Money</h2>
								<p className={`mt-2 text-sm ${muted}`}>
									Transfer funds securely between accounts.
								</p>
							</div>

							<form onSubmit={handleTransfer} className="max-w-xl space-y-5">
								<div>
									<label className="mb-2 block text-sm font-medium">
										From Account
									</label>
									<select
										required
										value={transferData.senderAccountId}
										onChange={(e) =>
											setTransferData({
												...transferData,
												senderAccountId: e.target.value,
											})
										}
										className={`w-full rounded-xl border p-3 outline-none ${dark
											? "border-white/10 bg-[#0D1422] text-white"
											: "border-slate-200 bg-white text-slate-800"
											}`}
									>
										<option value="">Select your account</option>
										{accounts.map(account => (
											<option key={account.id} value={account.id}>
												{account.accountNumber}
											</option>
										))}
									</select>
								</div>

								<div>
									<label className="mb-2 block text-sm font-medium">
										Receiver Account Number
									</label>

									<select
										required
										value={transferData.receiverAccountNumber}
										onChange={(e) =>
											setTransferData({
												...transferData,
												receiverAccountNumber: e.target.value,
											})
										}
										className={`w-full rounded-xl border p-3 outline-none ${dark
											? "border-white/10 bg-[#0D1422] text-white"
											: "border-slate-200 bg-white text-slate-800"
											}`}
									>
										<option value="">Select receiver account</option>

										{receiverAccounts
											.filter(
												(account) =>
													!accounts.some(
														(ownAccount) =>
															String(ownAccount.id) === String(account.id)
													)
											)
											.map((account) => (
												<option
													key={account.id}
													value={account.accountNumber}
												>
													{account.accountNumber}
												</option>
											))}
									</select>

									{receiverError && (
										<p className="mt-2 text-sm text-red-400">
											{receiverError}
										</p>
									)}
								</div>

								<div>
									<label className="mb-2 block text-sm font-medium">
										Amount (₹)
									</label>
									<input
										required
										type="number"
										min="0.01"
										step="0.01"
										value={transferData.amount}
										onChange={(e) =>
											setTransferData({
												...transferData,
												amount: e.target.value,
											})
										}
										placeholder="Enter transfer amount"
										className={`w-full rounded-xl border p-3 outline-none ${dark
											? "border-white/10 bg-[#0D1422] text-white"
											: "border-slate-200 bg-white text-slate-800"
											}`}
									/>
								</div>

								{transferMessage && (
									<p className="rounded-xl bg-emerald-500/10 p-3 text-sm text-emerald-500">
										{transferMessage}
									</p>
								)}

								{transferError && (
									<p className="rounded-xl bg-red-500/10 p-3 text-sm text-red-400">
										{transferError}
									</p>
								)}

								<button
									type="submit"
									disabled={transferLoading}
									className="flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 px-6 py-3 font-semibold text-white shadow-lg shadow-blue-500/20 disabled:cursor-not-allowed disabled:opacity-50"
								>
									<ArrowLeftRight size={18} />
									{transferLoading ? "Processing..." : "Transfer Money"}
								</button>
							</form>
						</section>
					)}

					{activePage !== "Transfer Money" &&
						activePage !== "AI Assistant" && (
							<section className="grid gap-5 xl:grid-cols-[1.5fr_1fr]">
								{/* Real Account Data */}
								<section>
									<div className="mb-4">
										<h2 className="text-lg font-bold">My Accounts</h2>
										<p className={`mt-1 text-sm ${muted}`}>
											Your linked bank accounts and balances
										</p>
									</div>

									{accountError && (
										<p className="rounded-xl border border-red-500/20 bg-red-500/10 p-4 text-sm text-red-400">
											{accountError}
										</p>
									)}

									{!accountError && accounts.length === 0 && (
										<p className={`rounded-xl border p-4 text-sm ${panel} ${muted}`}>
											No accounts found or accounts are loading.
										</p>
									)}

									<div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
										{accounts.map((account) => (
											<div
												key={account.id}
												className={`rounded-2xl border p-5 ${panel}`}
											>
												<div className="flex items-center justify-between">
													<Wallet size={20} className="text-cyan-500" />
													<span className="rounded-full bg-emerald-500/10 px-3 py-1 text-xs font-semibold text-emerald-500">
														{account.status}
													</span>
												</div>

												<p className={`mt-5 text-sm ${muted}`}>Account Number</p>
												<p className="mt-1 font-semibold">{account.accountNumber}</p>

												<p className={`mt-4 text-sm ${muted}`}>Available Balance</p>
												<p className="mt-1 text-2xl font-bold">
													₹{Number(account.balance).toLocaleString("en-IN", {
														minimumFractionDigits: 2,
													})}
												</p>
											</div>
										))}
									</div>
								</section>


								<div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#102B49] via-[#124C70] to-[#087E9B] p-6 text-white shadow-xl shadow-cyan-950/10 sm:p-8">
									<div className="pointer-events-none absolute -right-10 -top-20 h-64 w-64 rounded-full border border-white/10" />
									<div className="pointer-events-none absolute -right-2 -top-12 h-48 w-48 rounded-full border border-white/10" />
									<div className="pointer-events-none absolute -bottom-28 right-24 h-56 w-56 rounded-full bg-cyan-300/10 blur-3xl" />

									<div className="relative flex items-center justify-between gap-3">
										<div className="flex items-center gap-2 text-sm text-cyan-100">
											<Wallet size={18} />
											Total account balance
										</div>
										<span className="rounded-full border border-white/20 bg-white/10 px-3 py-1 text-xs">
											Overview
										</span>
									</div>

									<div className="relative mt-8">
										<p className="text-sm text-cyan-100">Available balance</p>
										<h2 className="mt-2 text-4xl font-bold tracking-tight sm:text-5xl">
											₹{totalBalance.toLocaleString("en-IN", {
												minimumFractionDigits: 2,
												maximumFractionDigits: 2,
											})}								</h2>
										<p className="mt-3 text-sm text-cyan-100/80">
											Demo balance · Connect your account to see real data
										</p>
									</div>

									<div className="relative mt-8 flex flex-wrap items-center justify-between gap-4 border-t border-white/15 pt-5">
										<div>
											<p className="text-xs text-cyan-100/80">Account holder</p>
											<p className="mt-1 font-semibold">{localStorage.getItem("username") || "User"}</p>
										</div>
										<div className="flex items-center gap-2 rounded-xl border border-white/20 bg-white/10 px-3 py-2">
											<CreditCard size={18} />
											<span className="text-sm">•••• •••• •••• 1001</span>
										</div>
									</div>
								</div>

								<div className="grid grid-cols-2 gap-4">
									<div className={`rounded-2xl border p-6 transition-all duration-200 hover:-translate-y-1 ${panel}`}>
										<div className="mb-5 grid h-10 w-10 place-items-center rounded-xl bg-emerald-500/10 text-emerald-500">
											<ArrowDownLeft size={21} />
										</div>
										<p className={`text-sm ${muted}`}>Money received</p>
										<h3 className="mt-2 text-xl font-bold sm:text-2xl">
											₹{moneyReceived.toLocaleString("en-IN")}
										</h3>
										<p className="mt-2 text-xs text-emerald-500">Successful transactions</p>
									</div>

									<div className={`rounded-2xl border p-6 transition-all duration-200 hover:-translate-y-1 ${panel}`}>
										<div className="mb-5 grid h-10 w-10 place-items-center rounded-xl bg-rose-500/10 text-rose-500">
											<ArrowUpRight size={21} />
										</div>
										<p className={`text-sm ${muted}`}>Money sent</p>
										<h3 className="mt-2 text-xl font-bold sm:text-2xl">
											₹{moneySent.toLocaleString("en-IN")}
										</h3>
										<p className="mt-2 text-xs text-emerald-500">Successful transactions</p>
									</div>

									<div className={`col-span-2 rounded-2xl border p-5 ${panel}`}>
										<div className="flex items-center justify-between">
											<div>
												<p className={`text-sm ${muted}`}>Account status</p>
												<p className="mt-2 font-semibold">Your security matters</p>
											</div>
											<span className="flex items-center gap-2 rounded-full bg-emerald-500/10 px-3 py-2 text-xs font-semibold text-emerald-500">
												<span className="h-2 w-2 rounded-full bg-emerald-500" />
												Demo view
											</span>
										</div>
									</div>
								</div>
							</section>

						)}

					{/* Full Transactions Page */}
					{activePage === "Transactions" && (
						<section className={`rounded-2xl border p-6 ${panel}`}>
							<div className="mb-6">
								<h2 className="text-xl font-bold">All Transactions</h2>
								<p className={`mt-2 text-sm ${muted}`}>
									Search and filter your complete transaction history.
								</p>
							</div>

							<div className="mb-6 grid gap-4 sm:grid-cols-2">
								<input
									type="text"
									placeholder="Search ID, type, status, or amount..."
									value={transactionSearch}
									onChange={(e) => setTransactionSearch(e.target.value)}
									className={`w-full rounded-xl border p-3 outline-none ${dark
										? "border-white/10 bg-[#0D1422] text-white"
										: "border-slate-200 bg-white text-slate-800"
										}`}
								/>

								<select
									value={transactionFilter}
									onChange={(e) => setTransactionFilter(e.target.value)}
									className={`w-full rounded-xl border p-3 outline-none ${dark
										? "border-white/10 bg-[#0D1422] text-white"
										: "border-slate-200 bg-white text-slate-800"
										}`}
								>
									<option value="ALL">All statuses</option>
									<option value="SUCCESS">Successful</option>
									<option value="PENDING">Pending</option>
									<option value="FAILED">Failed</option>
								</select>
							</div>

							<div className="mb-4 text-sm">
								<span className={muted}>
									Transactions found: {filteredTransactions.length}
								</span>
							</div>

							<div className="overflow-x-auto">
								<table className="w-full text-left text-sm">
									<thead className={muted}>
										<tr className="border-y border-slate-500/10">
											<th className="px-4 py-4 font-medium">Transaction ID</th>
											<th className="px-4 py-4 font-medium">Type</th>
											<th className="px-4 py-4 font-medium">Date</th>
											<th className="px-4 py-4 font-medium">Amount</th>
											<th className="px-4 py-4 font-medium">Status</th>
										</tr>
									</thead>

									<tbody>
										{filteredTransactions.map((tx) => (
											<tr
												key={tx.transactionId}
												className="border-b border-slate-500/10 last:border-b-0"
											>
												<td className="px-4 py-4 font-medium">
													{tx.transactionId}
												</td>

												<td className="px-4 py-4">
													{tx.type || "Transaction"}
												</td>

												<td className={`whitespace-nowrap px-4 py-4 ${muted}`}>
													{tx.createdAt
														? new Date(tx.createdAt).toLocaleString("en-IN")
														: "—"}
												</td>

												<td className="whitespace-nowrap px-4 py-4 font-semibold">
													₹{Number(tx.amount || 0).toLocaleString("en-IN", {
														minimumFractionDigits: 2,
														maximumFractionDigits: 2,
													})}
												</td>

												<td className="px-4 py-4">
													<span className="rounded-full bg-emerald-500/10 px-3 py-1 text-xs font-semibold text-emerald-500">
														{tx.status || "Unknown"}
													</span>
												</td>
											</tr>
										))}

										{filteredTransactions.length === 0 && (
											<tr>
												<td
													colSpan={5}
													className={`px-4 py-8 text-center ${muted}`}
												>
													No matching transactions found.
												</td>
											</tr>
										)}
									</tbody>
								</table>
							</div>
						</section>
					)}

					{/* Recent Transactions */}
					{activePage === "Dashboard" && (
						<section className={`overflow-hidden rounded-2xl border ${panel}`}>
							<div className="flex items-center justify-between p-6">
								<div>
									<h2 className="text-lg font-bold">Recent Transactions</h2>
									<p className={`mt-1 text-sm ${muted}`}>
										Your latest account activity
									</p>
								</div>

								<History size={20} className={muted} />
							</div>

							<div className="overflow-x-auto">
								<table className="w-full text-left text-sm">
									<thead className={muted}>
										<tr className="border-y border-slate-500/10">
											<th className="px-6 py-4 font-medium">Transaction</th>
											<th className="px-6 py-4 font-medium">Date</th>
											<th className="px-6 py-4 font-medium">Amount</th>
											<th className="px-6 py-4 font-medium">Status</th>
										</tr>
									</thead>

									<tbody>
										{transactionError && (
											<tr>
												<td colSpan={4} className="px-6 py-6 text-red-400">
													{transactionError}
												</td>
											</tr>
										)}

										{!transactionError && recentTransactions.length === 0 && (
											<tr>
												<td colSpan={4} className={`px-6 py-8 text-center ${muted}`}>
													No transactions found.
												</td>
											</tr>
										)}

										{recentTransactions.map((transaction) => (
											<tr
												key={transaction.transactionId}
												className="border-b border-slate-500/10 last:border-b-0"
											>
												<td className="px-6 py-4 font-medium">
													{transaction.type === "TRANSFER" &&
														transaction.receiverAccountId != null
														? `TRANSFER`
														: transaction.type || "Transaction"}											</td>

												<td className={`whitespace-nowrap px-6 py-4 ${muted}`}>
													{transaction.createdAt
														? new Date(transaction.createdAt).toLocaleString("en-IN", {
															day: "2-digit",
															month: "short",
															year: "numeric",
															hour: "numeric",
															minute: "2-digit",
															hour12: true,
														})
														: "—"}
												</td>

												<td className="whitespace-nowrap px-6 py-4 font-semibold">
													₹{Number(transaction.amount || 0).toLocaleString("en-IN", {
														minimumFractionDigits: 2,
														maximumFractionDigits: 2,
													})}
												</td>

												<td className="px-6 py-4">
													<span className="rounded-full bg-emerald-500/10 px-3 py-1 text-xs font-semibold text-emerald-500">
														{transaction.status || "Unknown"}
													</span>
												</td>
											</tr>
										))}
									</tbody>
								</table>
							</div>
						</section>
					)}
					<footer className={`pb-3 text-center text-xs ${muted}`}>
						VaultPay · Digital banking experience
					</footer>
				</div>
			</main >
		</div >
	);
}

export default Dashboard;