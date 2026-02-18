import { useState } from "react";
import Header from "./components/layout/Header";
import Dashboard from "./components/dashboard/Dashboard";
import EquipmentView from "./components/equipment/EquipmentView";
import ClientView from "./components/client/ClientView";
import RentalView from "./components/rental/RentalView";
import ReportsView from "./components/report/ReportsView";
import PaymentSection from "./components/payment/PaymentSection";
import "./styles/global.css";

function App() {

  const [view, setView] = useState("dashboard");

  const renderView = () => {
    switch (view) {
      case "equipments": return <EquipmentView />;
      case "clients": return <ClientView />;
      case "rentals": return <RentalView />;
      case "reports": return <ReportsView />;
      case "payments": return <PaymentSection />;
      default: return <Dashboard />;
    }
  };

  return (
    <div className="app-container">
      <Header view={view} setView={setView} />
      <main className="main-content">
        {renderView()}
      </main>
    </div>
  );
}

export default App;
