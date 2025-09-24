import React from "react";

export const Header: React.FC = () => {


    return (
            <nav className="bg-[#00095B] 800 p-4">
                <div className="container  flex">
                    <div className="justify-end items-center">

                    </div>
                    <div className='hidden md:block m-auto ml-28 flex-1 text-[24px] text-center'>
                        <div className='text-xl md:text-2xl text-white font-medium'>
                           Blue Agent
                        </div>
                    </div>
                </div>
            </nav>
    );
}