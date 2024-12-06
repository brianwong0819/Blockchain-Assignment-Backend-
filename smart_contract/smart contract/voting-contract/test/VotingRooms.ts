import { loadFixture, time } from "@nomicfoundation/hardhat-toolbox/network-helpers";
import { expect } from "chai";
import { ethers } from "hardhat";

describe("VotingRooms Contract", function () {
    // Fixture for deploying the VotingRooms contract
    async function deployVotingRoomsFixture() {
        const [owner, user1, user2] = await ethers.getSigners();
        const VotingRooms = await ethers.getContractFactory("VotingRooms");
        const votingRooms = await VotingRooms.deploy();
        await votingRooms.waitForDeployment();

        return { votingRooms, owner, user1, user2 };
    }

    describe("Deployment", function () {
        it("Should set the right name and symbol", async function () {
            const { votingRooms } = await loadFixture(deployVotingRoomsFixture);
            expect(await votingRooms.name()).to.equal("Voting Room Token");
            expect(await votingRooms.symbol()).to.equal("VRT");
        });
    });

    describe("Room Creation", function () {
        it("Should allow owner to create a room", async function () {
            const { votingRooms, owner } = await loadFixture(deployVotingRoomsFixture);
            const candidateIds = [1, 2, 3];
            await votingRooms.connect(owner).createRoom(10, candidateIds);

            const room = await votingRooms.rooms(1);
            expect(room.tokenLimit).to.equal(10);
            expect(room.votingOpen).to.equal(true);
        });

        it("Should emit RoomCreated event", async function () {
            const { votingRooms, owner } = await loadFixture(deployVotingRoomsFixture);
            const candidateIds = [1, 2, 3];

            await expect(votingRooms.connect(owner).createRoom(10, candidateIds))
                .to.emit(votingRooms, "RoomCreated")
                .withArgs(1, 10, candidateIds);
        });
    });

    describe("Token Distribution", function () {
        it("Should distribute tokens correctly", async function () {
            const { votingRooms, owner, user1 } = await loadFixture(deployVotingRoomsFixture);
            const candidateIds = [1, 2, 3];
            await votingRooms.connect(owner).createRoom(10, candidateIds);

            await votingRooms.connect(owner).distributeTokens(1, user1.address, 5);
            expect(await votingRooms.balanceOf(user1.address, 1)).to.equal(5);
        });

        it("Should revert if trying to distribute more than the token limit", async function () {
            const { votingRooms, owner, user1 } = await loadFixture(deployVotingRoomsFixture);
            const candidateIds = [1, 2, 3];
            await votingRooms.connect(owner).createRoom(10, candidateIds);

            await expect(
                votingRooms.connect(owner).distributeTokens(1, user1.address, 11)
            ).to.be.revertedWith("Token limit exceeded for this room");
        });
    });

    describe("Voting", function () {
        it("Should allow a user with tokens to vote", async function () {
            const { votingRooms, owner, user1 } = await loadFixture(deployVotingRoomsFixture);
            const candidateIds = [1, 2, 3];
            await votingRooms.connect(owner).createRoom(10, candidateIds);
            await votingRooms.connect(owner).distributeTokens(1, user1.address, 1);

            await votingRooms.connect(user1).vote(1, 1);
            expect(await votingRooms.getCandidateVotes(1, 1)).to.equal(1);
        });

        it("Should revert if trying to vote without tokens", async function () {
            const { votingRooms, user1 } = await loadFixture(deployVotingRoomsFixture);
            const candidateIds = [1, 2, 3];
            await votingRooms.createRoom(10, candidateIds);

            await expect(
                votingRooms.connect(user1).vote(1, 1)
            ).to.be.revertedWith("You do not have the required token for this room");
        });

        it("Should revert if voting is closed", async function () {
            const { votingRooms, owner, user1 } = await loadFixture(deployVotingRoomsFixture);
            const candidateIds = [1, 2, 3];
            await votingRooms.connect(owner).createRoom(10, candidateIds);
            await votingRooms.connect(owner).distributeTokens(1, user1.address, 1);

            // Close voting before voting
            await votingRooms.connect(owner).closeVoting(1);

            await expect(
                votingRooms.connect(user1).vote(1, 1)
            ).to.be.revertedWith("Voting is closed for this room");
        });
    });

    describe("Voting Management", function () {
        it("Should allow owner to close voting", async function () {
            const { votingRooms, owner } = await loadFixture(deployVotingRoomsFixture);
            const candidateIds = [1, 2, 3];
            await votingRooms.connect(owner).createRoom(10, candidateIds);

            await votingRooms.connect(owner).closeVoting(1);
            const room = await votingRooms.rooms(1);
            expect(room.votingOpen).to.equal(false);
        });

        it("Should allow owner to open voting", async function () {
            const { votingRooms, owner } = await loadFixture(deployVotingRoomsFixture);
            const candidateIds = [1, 2, 3];
            await votingRooms.connect(owner).createRoom(10, candidateIds);
            await votingRooms.connect(owner).closeVoting(1);
            await votingRooms.connect(owner).openVoting(1);

            const room = await votingRooms.rooms(1);
            expect(room.votingOpen).to.equal(true);
        });
    });

    describe("Room Results", function () {
        it("Should return the correct vote counts for candidates", async function () {
            const { votingRooms, owner, user1 } = await loadFixture(deployVotingRoomsFixture);
            const candidateIds = [1, 2, 3];
            await votingRooms.connect(owner).createRoom(10, candidateIds);
            await votingRooms.connect(owner).distributeTokens(1, user1.address, 3);

            await votingRooms.connect(user1).vote(1, 1);
            await votingRooms.connect(user1).vote(1, 2);
            await votingRooms.connect(user1).vote(1, 3);

            const [ids, counts] = await votingRooms.getRoomResults(1);
            expect(ids).to.deep.equal(candidateIds);
            expect(counts).to.deep.equal([1, 1, 1]);
        });
    });
});
