// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC1155/ERC1155.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract VotingRooms is ERC1155, Ownable {
    string public name = "Voting Room Token";
    string public symbol = "VRT";

    struct Room {
        uint256 tokenLimit;
        uint256 distributedTokens;
        uint256[] candidateIds;
        mapping(uint256 => uint256) candidateVotes;
    }

    mapping(uint256 => Room) public rooms;
    uint256 public roomCounter; 

    event RoomCreated(uint256 roomId, uint256 tokenLimit, uint256[] candidateIds);
    event TokenDistributed(uint256 roomId, address indexed user, uint256 amount);
    event VoteCast(uint256 roomId, uint256 candidateId, address indexed voter);

    constructor() ERC1155("") Ownable(msg.sender) {
    }

    function createRoom(uint256 roomId, uint256 tokenLimit, uint256[] memory candidateIds) public onlyOwner {
        require(rooms[roomId].tokenLimit == 0, "Room with this ID already exists");

        Room storage room = rooms[roomId];
        room.tokenLimit = tokenLimit;
        room.candidateIds = candidateIds;

        roomCounter++;

        emit RoomCreated(roomId, tokenLimit, candidateIds);
    }

    function distributeTokens(uint256 roomId, address to) public onlyOwner {
        Room storage room = rooms[roomId];
        uint256 amount = 1;

        require(room.distributedTokens + amount <= room.tokenLimit, "Token limit exceeded for this room");

        _mint(to, roomId, amount, "");
        room.distributedTokens += amount;

        emit TokenDistributed(roomId, to, amount);
    }

    function vote(uint256 roomId, uint256 candidateId) public {
        Room storage room = rooms[roomId];
        require(balanceOf(msg.sender, roomId) > 0, "You do not have the required token for this room");

        bool validCandidate = false;
        for (uint256 i = 0; i < room.candidateIds.length; i++) {
            if (room.candidateIds[i] == candidateId) {
                validCandidate = true;
                break;
            }
        }
        require(validCandidate, "Invalid candidate ID for this room");

        _burn(msg.sender, roomId, 1);
        room.candidateVotes[candidateId]++;

        emit VoteCast(roomId, candidateId, msg.sender);
    }

    function getCandidateVotes(uint256 roomId, uint256 candidateId) public view returns (uint256) {
        return rooms[roomId].candidateVotes[candidateId];
    }

    function getRoomResults(uint256 roomId) public view returns (uint256[] memory, uint256[] memory) {
        Room storage room = rooms[roomId];
        uint256[] memory counts = new uint256[](room.candidateIds.length);

        for (uint256 i = 0; i < room.candidateIds.length; i++) {
            counts[i] = room.candidateVotes[room.candidateIds[i]];
        }

        return (room.candidateIds, counts);
    }

    function getRoomDetails(uint256 roomId) public view returns (uint256, uint256[] memory) {
        Room storage room = rooms[roomId];
        return (room.tokenLimit, room.candidateIds);
    }

    function getRoomCount() public view returns (uint256) {
        return roomCounter;
    }

    function getUserTokenBalanceInRoom(uint256 roomId, address user) public view returns (uint256) {
        return balanceOf(user, roomId);
    }

    function getClosedRoomsDetails(uint256[] memory roomIds) 
    public 
    view 
    returns (
        uint256[] memory, 
        uint256[][] memory
    ) 
    {
        uint256[] memory selectedRoomIds = new uint256[](roomIds.length);
        uint256[][] memory candidateVotesList = new uint256[][](roomIds.length);

        for (uint256 i = 0; i < roomIds.length; i++) {
            uint256 roomId = roomIds[i];
            Room storage room = rooms[roomId];

            selectedRoomIds[i] = roomId;

            uint256[] memory votes = new uint256[](room.candidateIds.length);
            for (uint256 j = 0; j < room.candidateIds.length; j++) {
                votes[j] = room.candidateVotes[room.candidateIds[j]];
            }
            candidateVotesList[i] = votes;
        }

        return (selectedRoomIds, candidateVotesList);
    }

}
