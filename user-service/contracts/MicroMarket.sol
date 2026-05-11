// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC721/IERC721.sol";
import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/security/ReentrancyGuard.sol";

contract MicroMarket is Ownable, ReentrancyGuard {
    enum SaleType {
        FIXED_PRICE,
        DUTCH_AUCTION
    }

    struct Sale {
        uint256 saleId;
        address seller;
        address nftContract;
        uint256 tokenId;
        SaleType saleType;
        uint256 price;
        uint256 startPrice;
        uint256 reservePrice;
        uint256 startTime;
        uint256 endTime;
        address paymentToken;
        uint256 royaltyFee;
        address royaltyRecipient;
        bool active;
        bool completed;
    }

    struct Offer {
        uint256 offerId;
        address buyer;
        address nftContract;
        uint256 tokenId;
        uint256 price;
        address paymentToken;
        uint256 expirationTime;
        bool active;
        bool accepted;
    }

    uint256 private _saleIdCounter;
    uint256 private _offerIdCounter;

    mapping(uint256 => Sale) public sales;
    mapping(uint256 => Offer) public offers;
    mapping(address => mapping(uint256 => uint256)) public tokenToSale;
    mapping(address => mapping(uint256 => uint256[])) public tokenToOffers;

    event SaleCreated(uint256 indexed saleId, address indexed seller, address indexed nftContract, uint256 tokenId, SaleType saleType, uint256 price, uint256 endTime);
    event SaleExecuted(uint256 indexed saleId, address indexed seller, address indexed buyer, uint256 tokenId, uint256 price);
    event SaleCancelled(uint256 indexed saleId);
    event OfferCreated(uint256 indexed offerId, address indexed buyer, address indexed nftContract, uint256 tokenId, uint256 price);
    event OfferAccepted(uint256 indexed offerId, address indexed seller);
    event OfferCancelled(uint256 indexed offerId);

    constructor() Ownable() {}

    function createFixedPriceSale(
        address nftContract,
        uint256 tokenId,
        uint256 price,
        address paymentToken,
        uint256 duration,
        uint256 royaltyFee,
        address royaltyRecipient
    ) external returns (uint256) {
        require(price > 0, "Price must be greater than 0");
        require(duration > 0, "Duration must be greater than 0");

        IERC721 nft = IERC721(nftContract);
        require(nft.ownerOf(tokenId) == msg.sender, "Not the owner");
        nft.transferFrom(msg.sender, address(this), tokenId);

        uint256 saleId = _saleIdCounter;
        _saleIdCounter++;

        uint256 endTime = block.timestamp + duration;

        sales[saleId] = Sale({
            saleId: saleId,
            seller: msg.sender,
            nftContract: nftContract,
            tokenId: tokenId,
            saleType: SaleType.FIXED_PRICE,
            price: price,
            startPrice: price,
            reservePrice: price,
            startTime: block.timestamp,
            endTime: endTime,
            paymentToken: paymentToken,
            royaltyFee: royaltyFee,
            royaltyRecipient: royaltyRecipient,
            active: true,
            completed: false
        });

        tokenToSale[nftContract][tokenId] = saleId;
        emit SaleCreated(saleId, msg.sender, nftContract, tokenId, SaleType.FIXED_PRICE, price, endTime);
        return saleId;
    }

    function createDutchAuction(
        address nftContract,
        uint256 tokenId,
        uint256 startPrice,
        uint256 reservePrice,
        address paymentToken,
        uint256 duration,
        uint256 royaltyFee,
        address royaltyRecipient
    ) external returns (uint256) {
        require(startPrice > 0, "Start price must be greater than 0");
        require(startPrice >= reservePrice, "Start price must be >= reserve price");
        require(duration > 0, "Duration must be greater than 0");

        IERC721 nft = IERC721(nftContract);
        require(nft.ownerOf(tokenId) == msg.sender, "Not the owner");
        nft.transferFrom(msg.sender, address(this), tokenId);

        uint256 saleId = _saleIdCounter;
        _saleIdCounter++;
        uint256 endTime = block.timestamp + duration;

        sales[saleId] = Sale({
            saleId: saleId,
            seller: msg.sender,
            nftContract: nftContract,
            tokenId: tokenId,
            saleType: SaleType.DUTCH_AUCTION,
            price: startPrice,
            startPrice: startPrice,
            reservePrice: reservePrice,
            startTime: block.timestamp,
            endTime: endTime,
            paymentToken: paymentToken,
            royaltyFee: royaltyFee,
            royaltyRecipient: royaltyRecipient,
            active: true,
            completed: false
        });

        tokenToSale[nftContract][tokenId] = saleId;
        emit SaleCreated(saleId, msg.sender, nftContract, tokenId, SaleType.DUTCH_AUCTION, startPrice, endTime);
        return saleId;
    }

    function buy(uint256 saleId) external payable nonReentrant {
        Sale storage sale = sales[saleId];
        require(sale.active, "Sale is not active");
        require(block.timestamp <= sale.endTime, "Sale has ended");
        require(!sale.completed, "Sale is completed");

        uint256 currentPrice = _getCurrentPrice(sale);

        if (sale.paymentToken == address(0)) {
            require(msg.value >= currentPrice, "Insufficient payment");
        } else {
            require(msg.value == 0, "Cannot send MATIC for ERC20 sale");
            IERC20 token = IERC20(sale.paymentToken);
            require(token.transferFrom(msg.sender, address(this), currentPrice), "Token transfer failed");
        }

        _executeSale(saleId, msg.sender, currentPrice);
    }

    function cancelSale(uint256 saleId) external {
        Sale storage sale = sales[saleId];
        require(sale.active, "Sale is not active");
        require(sale.seller == msg.sender, "Only seller can cancel");
        require(!sale.completed, "Sale is completed");

        IERC721 nft = IERC721(sale.nftContract);
        nft.safeTransferFrom(address(this), sale.seller, sale.tokenId);
        sale.active = false;
        emit SaleCancelled(saleId);
    }

    function createOffer(
        address nftContract,
        uint256 tokenId,
        uint256 price,
        address paymentToken,
        uint256 duration
    ) external returns (uint256) {
        require(price > 0, "Price must be greater than 0");
        require(duration > 0, "Duration must be greater than 0");

        uint256 offerId = _offerIdCounter;
        _offerIdCounter++;
        uint256 expirationTime = block.timestamp + duration;

        offers[offerId] = Offer({
            offerId: offerId,
            buyer: msg.sender,
            nftContract: nftContract,
            tokenId: tokenId,
            price: price,
            paymentToken: paymentToken,
            expirationTime: expirationTime,
            active: true,
            accepted: false
        });

        tokenToOffers[nftContract][tokenId].push(offerId);
        emit OfferCreated(offerId, msg.sender, nftContract, tokenId, price);
        return offerId;
    }

    function acceptOffer(uint256 offerId) external nonReentrant {
        Offer storage offer = offers[offerId];
        require(offer.active, "Offer is not active");
        require(block.timestamp <= offer.expirationTime, "Offer has expired");
        require(!offer.accepted, "Offer already accepted");

        address nftOwner = IERC721(offer.nftContract).ownerOf(offer.tokenId);
        require(nftOwner == msg.sender, "Not the owner");

        IERC721(offer.nftContract).safeTransferFrom(msg.sender, offer.buyer, offer.tokenId);

        if (offer.paymentToken == address(0)) {
            // MATIC payment - simplified
        } else {
            IERC20 token = IERC20(offer.paymentToken);
            require(token.transferFrom(offer.buyer, msg.sender, offer.price), "Token transfer failed");
        }

        offer.accepted = true;
        offer.active = false;
        emit OfferAccepted(offerId, msg.sender);
    }

    function cancelOffer(uint256 offerId) external {
        Offer storage offer = offers[offerId];
        require(offer.active, "Offer is not active");
        require(offer.buyer == msg.sender, "Only buyer can cancel");
        offer.active = false;
        emit OfferCancelled(offerId);
    }

    function _getCurrentPrice(Sale storage sale) internal view returns (uint256) {
        if (sale.saleType == SaleType.FIXED_PRICE) {
            return sale.price;
        } else {
            if (block.timestamp >= sale.endTime) {
                return sale.reservePrice;
            }

            uint256 timeElapsed = block.timestamp - sale.startTime;
            uint256 totalTime = sale.endTime - sale.startTime;

            if (totalTime == 0) {
                return sale.startPrice;
            }

            uint256 priceDiff = sale.startPrice - sale.reservePrice;
            uint256 discount = (priceDiff * timeElapsed) / totalTime;
            return sale.startPrice - discount;
        }
    }

    function _executeSale(uint256 saleId, address buyer, uint256 price) internal {
        Sale storage sale = sales[saleId];
        uint256 royalty = (price * sale.royaltyFee) / 10000;
        uint256 sellerProceeds = price - royalty;

        IERC721 nft = IERC721(sale.nftContract);
        nft.safeTransferFrom(address(this), buyer, sale.tokenId);

        if (royalty > 0 && sale.royaltyRecipient != address(0)) {
            if (sale.paymentToken == address(0)) {
                payable(sale.royaltyRecipient).transfer(royalty);
            } else {
                IERC20(sale.paymentToken).transfer(sale.royaltyRecipient, royalty);
            }
        }

        if (sale.paymentToken == address(0)) {
            payable(sale.seller).transfer(sellerProceeds);
        } else {
            IERC20(sale.paymentToken).transfer(sale.seller, sellerProceeds);
        }

        sale.active = false;
        sale.completed = true;
        emit SaleExecuted(saleId, sale.seller, buyer, sale.tokenId, price);
    }

    function getSale(uint256 saleId) external view returns (Sale memory) {
        return sales[saleId];
    }

    function getCurrentPrice(uint256 saleId) external view returns (uint256) {
        Sale storage sale = sales[saleId];
        return _getCurrentPrice(sale);
    }

    function withdraw() external onlyOwner {
        payable(owner()).transfer(address(this).balance);
    }
}