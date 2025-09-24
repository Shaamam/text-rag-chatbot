import React from 'react';
import './App.css';
import { Header } from "./layout/header/Header";
import { Footer } from "./layout/footer/Footer";
import {ChatWindow} from "./components/chat/ChatWindow";

const App: React.FC = () => {
  return (
      <div className="App">
        <div className="min-h-screen flex flex-col justify-between overflow-visible bg-gray-50">
          <div className="sticky top-0 z-50" id="commonComponents">
            <Header/>
          </div>
          <main className="flex-1 w-full">
            <ChatWindow />
          </main>
          <Footer/>
        </div>
      </div>
  );
}

export default App;
