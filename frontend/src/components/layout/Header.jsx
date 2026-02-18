function Header({ view, setView }) {
  return (
    <>
      <header className="header">
        <h1>EcoRent Norte</h1>
        <span>Sistema de Gestión de Alquileres</span>
      </header>

      <nav className="navbar">
        <button
          className={view === "dashboard" ? "active" : ""}
          onClick={() => setView("dashboard")}
        >
          Dashboard
        </button>
        <button
          className={view === "equipments" ? "active" : ""}
          onClick={() => setView("equipments")}
        >
          Equipos
        </button>
        <button
          className={view === "clients" ? "active" : ""}
          onClick={() => setView("clients")}
        >
          Clientes
        </button>
        <button
          className={view === "rentals" ? "active" : ""}
          onClick={() => setView("rentals")}
        >
          Alquileres
        </button>
        <button
          className={view === "reports" ? "active" : ""}
          onClick={() => setView("reports")}
        >
          Reportes
        </button>
        <button
          className={view === "payments" ? "active" : ""}
          onClick={() => setView("payments")}
        >
          Pagos
        </button>
      </nav>
    </>
  );
}

export default Header;
