// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract NFTAsset is ERC721, ERC721URIStorage, Ownable {
    uint256 private _tokenIdCounter;

    struct TokenVersion {
        uint256 version;
        string metadataHash;
        string ipfsUri;
        uint256 timestamp;
        address updater;
    }

    struct RoyaltyInfo {
        address recipient;
        uint96 feeNumerator;
    }

    mapping(uint256 => TokenVersion[]) public versionHistory;
    mapping(uint256 => RoyaltyInfo) public royaltyInfo;

    event VersionUpdated(uint256 indexed tokenId, string newMetadataHash, uint256 version, uint256 timestamp);
    event RoyaltySet(uint256 indexed tokenId, address recipient, uint96 feeNumerator);

    constructor() ERC721("CreativeNFT", "CNFT") Ownable() {}

    function mint(address to, string memory metadataHash) public onlyOwner returns (uint256) {
        uint256 tokenId = _tokenIdCounter;
        _tokenIdCounter++;

        _safeMint(to, tokenId);
        _setTokenURI(tokenId, metadataHash);

        versionHistory[tokenId].push(TokenVersion({
            version: 1,
            metadataHash: metadataHash,
            ipfsUri: string.concat("ipfs://", metadataHash),
            timestamp: block.timestamp,
            updater: msg.sender
        }));

        royaltyInfo[tokenId] = RoyaltyInfo({
            recipient: to,
            feeNumerator: 500
        });

        emit VersionUpdated(tokenId, metadataHash, 1, block.timestamp);
        return tokenId;
    }

    function updateMetadata(uint256 tokenId, string memory newMetadataHash) public {
        require(ownerOf(tokenId) != address(0), "Token does not exist");
        address tokenOwner = ownerOf(tokenId);
        require(msg.sender == tokenOwner || getApproved(tokenId) == msg.sender || isApprovedForAll(tokenOwner, msg.sender) || msg.sender == owner(), "Not authorized");

        TokenVersion[] storage versions = versionHistory[tokenId];
        uint256 newVersion = versions.length + 1;

        versions.push(TokenVersion({
            version: newVersion,
            metadataHash: newMetadataHash,
            ipfsUri: string.concat("ipfs://", newMetadataHash),
            timestamp: block.timestamp,
            updater: msg.sender
        }));

        _setTokenURI(tokenId, newMetadataHash);
        emit VersionUpdated(tokenId, newMetadataHash, newVersion, block.timestamp);
    }

    function getVersionHistory(uint256 tokenId) public view returns (TokenVersion[] memory) {
        require(ownerOf(tokenId) != address(0), "Token does not exist");
        return versionHistory[tokenId];
    }

    function getCurrentVersion(uint256 tokenId) public view returns (uint256) {
        return versionHistory[tokenId].length;
    }

    function setRoyalty(uint256 tokenId, address recipient, uint96 feeNumerator) public {
        require(ownerOf(tokenId) != address(0), "Token does not exist");
        require(ownerOf(tokenId) == msg.sender || owner() == msg.sender, "Not authorized");
        require(feeNumerator <= 10000, "Fee cannot exceed 100%");

        royaltyInfo[tokenId] = RoyaltyInfo({
            recipient: recipient,
            feeNumerator: feeNumerator
        });

        emit RoyaltySet(tokenId, recipient, feeNumerator);
    }

    function getRoyaltyInfo(uint256 tokenId) public view returns (address, uint96) {
        RoyaltyInfo memory info = royaltyInfo[tokenId];
        return (info.recipient, info.feeNumerator);
    }

    function royaltyAmount(uint256 tokenId, uint256 salePrice) public view returns (uint256) {
        RoyaltyInfo memory info = royaltyInfo[tokenId];
        return (salePrice * info.feeNumerator) / 10000;
    }

    function tokenURI(uint256 tokenId) public view override(ERC721, ERC721URIStorage) returns (string memory) {
        return super.tokenURI(tokenId);
    }

    function supportsInterface(bytes4 interfaceId) public view override(ERC721, ERC721URIStorage) returns (bool) {
        return super.supportsInterface(interfaceId);
    }

    function burn(uint256 tokenId) public {
        address tokenOwner = ownerOf(tokenId);
        require(tokenOwner == msg.sender || getApproved(tokenId) == msg.sender || isApprovedForAll(tokenOwner, msg.sender) || msg.sender == owner(), "Not authorized");
        _burn(tokenId);
    }

    function _burn(uint256 tokenId) internal override(ERC721, ERC721URIStorage) {
        super._burn(tokenId);
    }
}