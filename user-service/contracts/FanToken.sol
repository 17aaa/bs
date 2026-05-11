// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/token/ERC20/extensions/ERC20Burnable.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/security/ReentrancyGuard.sol";

// 粉丝代币合约
contract FanToken is ERC20, ERC20Burnable, Ownable, ReentrancyGuard {
    struct TokenAllocation {
        uint256 publicSale;
        uint256 creator;
        uint256 team;
        uint256 staking;
    }

    struct VestingInfo {
        address beneficiary;
        uint256 totalAmount;
        uint256 released;
        uint256 startTime;
        uint256 cliffDuration;
        uint256 vestingDuration;
    }

    struct StakeInfo {
        uint256 stakedAmount;
        uint256 rewardDebt;
        uint256 lastUpdateTime;
    }

    struct SaleConfig {
        uint256 pricePerToken;
        uint256 minPurchase;
        uint256 maxPurchase;
        uint256 startTime;
        uint256 endTime;
        bool active;
    }

    address public creator;
    TokenAllocation public allocation;
    mapping(address => VestingInfo) public vestingInfo;
    mapping(address => StakeInfo) public stakeInfo;
    uint256 public stakingPool;
    uint256 public totalStaked;
    uint256 public rewardRate;
    SaleConfig public saleConfig;
    uint256 public publicSaleRaised;

    event TokensPurchased(address indexed buyer, uint256 amount, uint256 cost);
    event Staked(address indexed user, uint256 amount);
    event Unstaked(address indexed user, uint256 amount);
    event RewardClaimed(address indexed user, uint256 amount);
    event VestingReleased(address indexed beneficiary, uint256 amount);
    event SaleConfigUpdated(SaleConfig config);

    constructor(
        string memory name,
        string memory symbol,
        address _creator,
        uint256 totalSupply,
        TokenAllocation memory _allocation
    ) ERC20(name, symbol) Ownable() {
        require(_allocation.publicSale + _allocation.creator + _allocation.team + _allocation.staking == 100, "Invalid allocation");

        creator = _creator;
        allocation = _allocation;

        uint256 totalAmount = totalSupply * 1e18;
        _mint(address(this), totalAmount);

        uint256 publicSaleAmount = (totalAmount * _allocation.publicSale) / 100;
        uint256 creatorAmount = (totalAmount * _allocation.creator) / 100;
        uint256 teamAmount = (totalAmount * _allocation.team) / 100;
        uint256 stakingAmount = (totalAmount * _allocation.staking) / 100;

        vestingInfo[creator] = VestingInfo({
            beneficiary: creator,
            totalAmount: creatorAmount,
            released: 0,
            startTime: block.timestamp,
            cliffDuration: 0,
            vestingDuration: 365 days * 4
        });

        vestingInfo[address(this)] = VestingInfo({
            beneficiary: address(this),
            totalAmount: teamAmount,
            released: 0,
            startTime: block.timestamp,
            cliffDuration: 0,
            vestingDuration: 365 days
        });

        stakingPool = stakingAmount;
        _transfer(address(this), address(this), stakingAmount);
        _transfer(address(this), address(this), publicSaleAmount);
    }

    function configureSale(
        uint256 pricePerToken,
        uint256 minPurchase,
        uint256 maxPurchase,
        uint256 startTime,
        uint256 endTime
    ) external onlyOwner {
        require(startTime >= block.timestamp, "Invalid start time");
        require(endTime > startTime, "Invalid end time");

        saleConfig = SaleConfig({
            pricePerToken: pricePerToken,
            minPurchase: minPurchase,
            maxPurchase: maxPurchase,
            startTime: startTime,
            endTime: endTime,
            active: true
        });

        emit SaleConfigUpdated(saleConfig);
    }

    function participateInSale(uint256 amount) external payable nonReentrant {
        require(saleConfig.active, "Sale is not active");
        require(block.timestamp >= saleConfig.startTime, "Sale has not started");
        require(block.timestamp <= saleConfig.endTime, "Sale has ended");

        uint256 cost = (amount * saleConfig.pricePerToken) / 1e18;
        require(msg.value >= cost, "Insufficient payment");

        require(amount >= saleConfig.minPurchase, "Below minimum purchase");
        require(amount <= saleConfig.maxPurchase, "Exceeds maximum purchase");

        uint256 balance = balanceOf(address(this));
        require(amount <= balance, "Insufficient tokens for sale");

        publicSaleRaised += cost;
        _transfer(address(this), msg.sender, amount);

        emit TokensPurchased(msg.sender, amount, cost);

        if (msg.value > cost) {
            payable(msg.sender).transfer(msg.value - cost);
        }
    }

    function stake(uint256 amount) external nonReentrant {
        require(amount > 0, "Amount must be greater than 0");
        require(amount <= balanceOf(msg.sender), "Insufficient balance");

        StakeInfo storage info = stakeInfo[msg.sender];
        _updateReward(msg.sender);

        info.stakedAmount += amount;
        totalStaked += amount;
        _transfer(msg.sender, address(this), amount);

        emit Staked(msg.sender, amount);
    }

    function unstake(uint256 amount) external nonReentrant {
        StakeInfo storage info = stakeInfo[msg.sender];
        require(info.stakedAmount >= amount, "Insufficient staked amount");

        _updateReward(msg.sender);
        info.stakedAmount -= amount;
        totalStaked -= amount;
        _transfer(address(this), msg.sender, amount);

        emit Unstaked(msg.sender, amount);
    }

    function getReward() external nonReentrant returns (uint256) {
        StakeInfo storage info = stakeInfo[msg.sender];
        _updateReward(msg.sender);

        uint256 reward = info.rewardDebt;
        require(reward > 0, "No reward to claim");

        uint256 poolBalance = balanceOf(address(this));
        if (reward > poolBalance) {
            reward = poolBalance;
        }

        info.rewardDebt = 0;
        _transfer(address(this), msg.sender, reward);

        emit RewardClaimed(msg.sender, reward);
        return reward;
    }

    function claimVested() external nonReentrant {
        VestingInfo storage info = vestingInfo[msg.sender];
        require(info.totalAmount > 0, "No vesting info");

        uint256 vested = _calculateVested(info);
        require(vested > info.released, "No tokens to release");

        uint256 amount = vested - info.released;
        info.released = vested;
        _transfer(address(this), msg.sender, amount);

        emit VestingReleased(msg.sender, amount);
    }

    function _calculateVested(VestingInfo storage info) internal view returns (uint256) {
        if (block.timestamp < info.startTime + info.cliffDuration) {
            return 0;
        }

        if (block.timestamp >= info.startTime + info.vestingDuration) {
            return info.totalAmount;
        }

        uint256 timeElapsed = block.timestamp - info.startTime - info.cliffDuration;
        uint256 vestingTime = info.vestingDuration - info.cliffDuration;
        return (info.totalAmount * timeElapsed) / vestingTime;
    }

    function _updateReward(address user) internal {
        StakeInfo storage info = stakeInfo[user];

        if (info.stakedAmount > 0 && rewardRate > 0) {
            uint256 timeElapsed = block.timestamp - info.lastUpdateTime;
            uint256 reward = (info.stakedAmount * rewardRate * timeElapsed) / totalStaked / 1e18;
            info.rewardDebt += reward;
        }

        info.lastUpdateTime = block.timestamp;
    }

    function setRewardRate(uint256 _rewardRate) external onlyOwner {
        rewardRate = _rewardRate;
    }

    function getPendingReward(address user) external view returns (uint256) {
        StakeInfo storage info = stakeInfo[user];

        if (info.stakedAmount == 0 || rewardRate == 0) {
            return info.rewardDebt;
        }

        uint256 timeElapsed = block.timestamp - info.lastUpdateTime;
        uint256 pendingReward = (info.stakedAmount * rewardRate * timeElapsed) / totalStaked / 1e18;
        return info.rewardDebt + pendingReward;
    }

    function getVestedAmount(address user) external view returns (uint256) {
        VestingInfo storage info = vestingInfo[user];
        uint256 vested = _calculateVested(info);
        return vested > info.released ? vested - info.released : 0;
    }

    function withdrawTokens(address token, uint256 amount) external onlyOwner {
        require(token != address(this), "Cannot withdraw native tokens");
        IERC20(token).transfer(owner(), amount);
    }
}