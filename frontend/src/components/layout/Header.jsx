function Header({ setView }) {
  return (
    <>
      <header className="header">
        <h1>EcoRent Norte</h1>
        <span>Sistema de Gestión de Alquileres</span>
      </header>

      <nav className="navbar">
        <button onClick={() => setView("dashboard")}>Dashboard</button>
        <button onClick={() => setView("equipments")}>Equipos</button>
        <button onClick={() => setView("clients")}>Clientes</button>
        <button onClick={() => setView("rentals")}>Alquileres</button>
        <button onClick={() => setView("reports")}>Reportes</button>
        <button onClick={() => setView("payments")}>Pagos</button>
      </nav>
    </>
  );
}

export default Header;
