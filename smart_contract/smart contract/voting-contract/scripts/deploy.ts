import { ethers } from "hardhat";

async function main() {
    const VotingRooms = await ethers.getContractFactory("VotingRooms");

    const votingRooms = await VotingRooms.deploy();
    await votingRooms.waitForDeployment();  

    console.log("VotingRooms deployed to:", votingRooms.target);
}

main().catch((error) => {
    console.error(error);
    process.exitCode = 1;
});
