import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import axios from 'axios';
import { nftApi, uploadApi, ipfsApi } from '../services/api';
import { notify } from '../components/Notification';
import Header from '../components/Header';

const Container = styled.div`
  min-height: 100vh;
  background: #f5f7fa;
`;

const Content = styled.div`
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 24px;

  @media (max-width: 768px) {
    padding: 24px 16px;
  }
`;

const Title = styled.h1`
  font-size: 32px;
  margin: 0 0 32px 0;
  color: #333;
  text-align: center;
`;

const Form = styled.form`
  background: white;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
`;

const FormGroup = styled.div`
  margin-bottom: 24px;
`;

const Label = styled.label`
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;

  @media (max-width: 768px) {
    font-size: 13px;
  }
`;

const Input = styled.input`
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.2s ease;

  &:focus {
    outline: none;
    border-color: #667eea;
  }
`;

const Textarea = styled.textarea`
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
  resize: vertical;
  min-height: 120px;
  transition: border-color 0.2s ease;

  &:focus {
    outline: none;
    border-color: #667eea;
  }
`;

const Select = styled.select`
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.2s ease;

  &:focus {
    outline: none;
    border-color: #667eea;
  }
`;

const SubmitButton = styled.button`
  width: 100%;
  padding: 16px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    transform: none;
  }

  @media (max-width: 768px) {
    font-size: 14px;
    padding: 14px;
  }
`;

const Hint = styled.span`
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  display: block;
`;

const UploadArea = styled.div`
  border: 2px dashed #e0e0e0;
  border-radius: 8px;
  padding: 32px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: #667eea;
    background: #f5f7fa;
  }
`;

const UploadIcon = styled.div`
  font-size: 48px;
  margin-bottom: 16px;
`;

const PreviewImage = styled.img`
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
  margin-top: 16px;
`;

const PreviewVideo = styled.video`
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
  margin-top: 16px;
`;

const PreviewPdf = styled.iframe`
  width: 100%;
  height: 300px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-top: 16px;
`;

const PreviewOther = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 200px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-top: 16px;
  color: #666;
`;

const PreviewOffice = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 200px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  border-radius: 8px;
  margin-top: 16px;
  color: #666;
  border: 2px dashed #d0d7de;
`;

const PreviewModel = styled.div`
  width: 100%;
  height: 300px;
  border-radius: 8px;
  margin-top: 16px;
  background: #1a1a2e;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
`;

const FileTypeInfo = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  padding: 8px 16px;
  background: #e3f2fd;
  border-radius: 4px;
  font-size: 13px;
  color: #1976d2;
`;

const UploadProgress = styled.div`
  margin-top: 12px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  height: 8px;
`;

const ProgressBar = styled.div`
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
  width: ${(props) => props.progress}%;
`;

const ProgressText = styled.p`
  font-size: 12px;
  color: #999;
  margin-top: 8px;
`;

const ImageActions = styled.div`
  display: flex;
  gap: 8px;
  margin-top: 12px;
  justify-content: center;
`;

const ActionButton = styled.button`
  padding: 6px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background: white;
  color: #666;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: #667eea;
    color: #667eea;
  }
`;

const StorageOption = styled.div`
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
`;

const StorageButton = styled.button`
  flex: 1;
  padding: 12px 16px;
  border: 2px solid ${(props) => (props.$active ? '#667eea' : '#e0e0e0')};
  border-radius: 8px;
  background: ${(props) => (props.$active ? '#f0f4ff' : 'white')};
  color: ${(props) => (props.$active ? '#667eea' : '#666')};
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;

  &:hover {
    border-color: #667eea;
  }
`;

const IpfsBadge = styled.span`
  padding: 2px 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 4px;
  font-size: 10px;
  font-weight: bold;
`;

const MinioBadge = styled.span`
  padding: 2px 8px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border-radius: 4px;
  font-size: 10px;
  font-weight: bold;
`;

// 步骤指示器样式
const StepsContainer = styled.div`
  margin-bottom: 32px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
`;

const StepsTitle = styled.h3`
  margin: 0 0 20px 0;
  font-size: 16px;
  color: #333;
  text-align: center;
`;

const StepsWrapper = styled.div`
  display: flex;
  justify-content: space-between;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 20px;
    left: 10%;
    right: 10%;
    height: 2px;
    background: #e0e0e0;
    z-index: 1;
  }
`;

const Step = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 2;
  flex: 1;
`;

const StepCircle = styled.div`
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  background: ${(props) => {
    if (props.$status === 'completed') return 'linear-gradient(135deg, #10b981 0%, #059669 100%)';
    if (props.$status === 'processing') return 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
    if (props.$status === 'failed') return 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)';
    return '#e0e0e0';
  }};
  color: ${(props) => (props.$status === 'pending' ? '#999' : 'white')};
  transition: all 0.3s ease;
  animation: ${(props) => props.$status === 'processing' ? 'pulse 1.5s infinite' : 'none'};

  @keyframes pulse {
    0%, 100% { transform: scale(1); }
    50% { transform: scale(1.1); }
  }
`;

const StepLabel = styled.span`
  margin-top: 8px;
  font-size: 12px;
  color: ${(props) => {
    if (props.$status === 'completed') return '#10b981';
    if (props.$status === 'processing') return '#667eea';
    if (props.$status === 'failed') return '#ef4444';
    return '#999';
  }};
  font-weight: ${(props) => props.$status === 'processing' ? '600' : '400'};
  text-align: center;

  @media (max-width: 600px) {
    font-size: 10px;
  }
`;

const ProgressContainer = styled.div`
  margin-top: 20px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
`;

const ProgressHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
`;

const ProgressLabel = styled.span`
  font-size: 14px;
  color: #333;
  font-weight: 500;
`;

const ProgressPercent = styled.span`
  font-size: 14px;
  color: #667eea;
  font-weight: 600;
`;

const MainProgressBar = styled.div`
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
`;

const MainProgressFill = styled.div`
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
  width: ${(props) => props.progress}%;
`;

const ErrorMessage = styled.div`
  margin-top: 12px;
  padding: 12px;
  background: #fef2f2;
  border-radius: 8px;
  color: #dc2626;
  font-size: 14px;
`;

const SuccessMessage = styled.div`
  margin-top: 12px;
  padding: 12px;
  background: #f0fdf4;
  border-radius: 8px;
  color: #059669;
  font-size: 14px;
  text-align: center;
`;

const ActionButtons = styled.div`
  display: flex;
  gap: 12px;
  margin-top: 16px;
  justify-content: center;
`;

const RetryButton = styled.button`
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    transform: none;
  }
`;

const CancelButton = styled.button`
  padding: 10px 24px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  color: #666;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: #ef4444;
    color: #ef4444;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`;

const RetryInfo = styled.div`
  margin-top: 8px;
  font-size: 12px;
  color: #999;
  text-align: center;
`;

const categories = [
  { value: 'Art', label: '艺术' },
  { value: 'Music', label: '音乐' },
  { value: 'Photography', label: '摄影' },
  { value: 'Design', label: '设计' },
  { value: 'Writing', label: '写作' },
  { value: 'Video', label: '视频' },
  { value: 'Other', label: '其他' },
];

const stepNames = ['文件上传', '元数据生成', '链上铸造', '铸造完成'];

// 文件类型图标映射
const fileTypeIcons = {
  image: '🖼️',
  video: '🎬',
  audio: '🎵',
  pdf: '📕',
  word: '📘',
  excel: '📗',
  ppt: '📙',
  zip: '📦',
  model3d: '🎮',
  code: '💻',
  other: '📄',
};

// 根据URL或文件名判断文件类型
const getFileType = (url) => {
  if (!url) return 'other';
  const ext = url.toLowerCase().split('?')[0].split('.').pop();

  // 图片
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp', 'ico', 'tiff', 'tif', 'heic', 'heif'].includes(ext)) return 'image';

  // 视频
  if (['mp4', 'webm', 'ogg', 'mov', 'avi', 'mkv', 'wmv', 'flv', 'm4v', '3gp'].includes(ext)) return 'video';

  // 音频
  if (['mp3', 'wav', 'ogg', 'flac', 'aac', 'm4a', 'wma', 'aiff', 'ape'].includes(ext)) return 'audio';

  // PDF
  if (ext === 'pdf') return 'pdf';

  // Word文档
  if (['doc', 'docx', 'rtf', 'odt'].includes(ext)) return 'word';

  // Excel表格
  if (['xls', 'xlsx', 'csv', 'ods'].includes(ext)) return 'excel';

  // PPT演示
  if (['ppt', 'pptx', 'odp'].includes(ext)) return 'ppt';

  // 压缩文件
  if (['zip', 'rar', '7z', 'tar', 'gz', 'bz2', 'xz'].includes(ext)) return 'zip';

  // 3D模型
  if (['glb', 'gltf', 'obj', 'fbx', 'stl', 'dae', '3ds', 'blend'].includes(ext)) return 'model3d';

  // 代码文件
  if (['js', 'ts', 'jsx', 'tsx', 'py', 'java', 'c', 'cpp', 'h', 'cs', 'go', 'rs', 'rb', 'php', 'swift', 'kt', 'scala', 'vue', 'html', 'css', 'scss', 'less', 'json', 'xml', 'yaml', 'yml', 'md', 'sql'].includes(ext)) return 'code';

  return 'other';
};

// 获取文件类型显示名称
const getFileTypeName = (type) => {
  const names = {
    image: '图片',
    video: '视频',
    audio: '音频',
    pdf: 'PDF文档',
    word: 'Word文档',
    excel: 'Excel表格',
    ppt: 'PPT演示',
    zip: '压缩文件',
    model3d: '3D模型',
    code: '代码文件',
    other: '文件',
  };
  return names[type] || '文件';
};

// 文件预览组件
const FilePreview = ({ url, fileName }) => {
  const type = getFileType(url);

  if (!url) return null;

  // 图片预览
  if (type === 'image') {
    return <PreviewImage src={url} alt="预览" />;
  }

  // 视频预览
  if (type === 'video') {
    return <PreviewVideo src={url} controls />;
  }

  // PDF预览
  if (type === 'pdf') {
    return <PreviewPdf src={url} title="PDF预览" />;
  }

  // 音频预览
  if (type === 'audio') {
    return (
      <PreviewOther>
        <div style={{ fontSize: '48px', marginBottom: '8px' }}>🎵</div>
        <div>音频文件</div>
        <audio controls src={url} style={{ marginTop: '12px' }} />
      </PreviewOther>
    );
  }

  // Office文件预览（Word、Excel、PPT）
  if (['word', 'excel', 'ppt'].includes(type)) {
    return (
      <>
        <PreviewOffice>
          <div style={{ fontSize: '64px', marginBottom: '12px' }}>
            {fileTypeIcons[type]}
          </div>
          <div style={{ fontSize: '16px', fontWeight: 500, marginBottom: '4px' }}>
            {getFileTypeName(type)}
          </div>
          <div style={{ fontSize: '13px', color: '#888' }}>
            {fileName || '文件已上传'}
          </div>
        </PreviewOffice>
        <FileTypeInfo>
          <span>💡 {getFileTypeName(type)}已上传成功，买家购买后可下载查看完整内容</span>
        </FileTypeInfo>
      </>
    );
  }

  // 压缩文件
  if (type === 'zip') {
    return (
      <>
        <PreviewOther>
          <div style={{ fontSize: '64px', marginBottom: '12px' }}>📦</div>
          <div style={{ fontSize: '16px', fontWeight: 500 }}>压缩文件</div>
          <div style={{ fontSize: '13px', color: '#888', marginTop: '4px' }}>
            {fileName || '文件已上传'}
          </div>
        </PreviewOther>
        <FileTypeInfo>
          <span>💡 压缩包已上传，买家购买后可下载</span>
        </FileTypeInfo>
      </>
    );
  }

  // 3D模型
  if (type === 'model3d') {
    return (
      <>
        <PreviewModel>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '64px', marginBottom: '12px' }}>🎮</div>
            <div>3D模型预览</div>
            <div style={{ fontSize: '12px', color: '#aaa', marginTop: '8px' }}>
              支持 GLB、GLTF、OBJ 等格式
            </div>
          </div>
        </PreviewModel>
        <FileTypeInfo>
          <span>💡 3D模型已上传，买家购买后可下载查看</span>
        </FileTypeInfo>
      </>
    );
  }

  // 代码文件
  if (type === 'code') {
    return (
      <>
        <PreviewOther>
          <div style={{ fontSize: '64px', marginBottom: '12px' }}>💻</div>
          <div style={{ fontSize: '16px', fontWeight: 500 }}>代码文件</div>
          <div style={{ fontSize: '13px', color: '#888', marginTop: '4px' }}>
            {fileName || '文件已上传'}
          </div>
        </PreviewOther>
        <FileTypeInfo>
          <span>💡 代码文件已上传，买家购买后可下载查看源码</span>
        </FileTypeInfo>
      </>
    );
  }

  // 其他文件类型
  return (
    <PreviewOther>
      <div style={{ fontSize: '48px', marginBottom: '8px' }}>📄</div>
      <div>{fileName || '文件已上传'}</div>
    </PreviewOther>
  );
};

function CreatePage() {
  const navigate = useNavigate();
  const fileInputRef = useRef(null);
  const [account, setAccount] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    category: 'Art',
    royaltyFee: 500,
    imageUrl: '',
  });
  const [previewUrl, setPreviewUrl] = useState(null);
  const [fileType, setFileType] = useState(null); // 'image', 'video', 'pdf', 'other'
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [storageType, setStorageType] = useState('minio'); // minio 或 ipfs
  const [ipfsCid, setIpfsCid] = useState(null);

  // 铸造进度状态
  const [mintTask, setMintTask] = useState(null);
  const [showProgress, setShowProgress] = useState(false);

  // 轮询铸造进度
  useEffect(() => {
    if (!mintTask || mintTask.completed || mintTask.failed) return;

    const pollInterval = setInterval(async () => {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(`/api/nft/mint-task/${mintTask.taskId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (response.data?.status === 'success') {
          const task = response.data.data;
          setMintTask(task);

          if (task.completed) {
            clearInterval(pollInterval);
            notify.success('NFT 铸造成功！');
            setTimeout(() => {
              navigate(`/nft/${task.nftAssetId}`);
            }, 1500);
          } else if (task.failed) {
            clearInterval(pollInterval);
            notify.error(`铸造失败：${task.errorMessage}`);
            setSubmitting(false);
          }
        }
      } catch (error) {
        console.error('轮询铸造进度失败:', error);
      }
    }, 1000);

    return () => clearInterval(pollInterval);
  }, [mintTask, navigate]);

  const handleConnect = (acc) => {
    setAccount(acc);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'royaltyFee' ? parseInt(value) || 0 : value,
    }));
  };

  const handleImageUpload = async (e) => {
    const file = e.target.files[0];
    if (file) {
      await uploadFile(file);
    }
  };

  const uploadFile = async (file) => {
    const maxSize = 100 * 1024 * 1024; // 统一最大100MB

    if (file.size > maxSize) {
      notify.warning('文件大小不能超过 100MB');
      return;
    }

    const previewUrl = URL.createObjectURL(file);
    setPreviewUrl(previewUrl);
    setIsUploading(true);
    setUploadProgress(0);
    setIpfsCid(null);

    const progressInterval = setInterval(() => {
      setUploadProgress((prev) => {
        if (prev >= 90) {
          clearInterval(progressInterval);
          return 90;
        }
        return prev + 10;
      });
    }, 200);

    try {
      let response;
      let imageUrl;

      if (storageType === 'ipfs') {
        notify.info('正在上传到 IPFS...');
        response = await ipfsApi.uploadImage(file);

        if (response.status === 'success' && response.data) {
          imageUrl = response.data.url;
          setIpfsCid(response.data.cid);
          notify.success('文件已上传到 IPFS');
        }
      } else {
        // 使用 MinIO 统一上传接口
        notify.info('正在上传到对象存储...');
        const formDataObj = new FormData();
        formDataObj.append('file', file);
        response = await axios.post('/api/upload/nft/asset', formDataObj, {
          headers: { 'Content-Type': 'multipart/form-data' }
        }).then(res => res.data);

        if (response.status === 'success' && response.data) {
          imageUrl = response.data.url;
          notify.success('文件已上传到对象存储');
        }
      }

      if (imageUrl) {
        setPreviewUrl(imageUrl); // 使用服务器返回的URL预览
        setFileType(getFileType(imageUrl));
        setFormData((prev) => ({
          ...prev,
          imageUrl: imageUrl,
        }));
      }

      clearInterval(progressInterval);
      setUploadProgress(100);
      setIsUploading(false);
    } catch (error) {
      clearInterval(progressInterval);
      console.error('上传失败:', error);
      notify.warning('文件上传失败，将使用本地预览');
      setIsUploading(false);
      setUploadProgress(0);
      setFormData((prev) => ({
        ...prev,
        imageUrl: previewUrl,
      }));
    }
  };

  const handleDrop = async (e) => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (file) {
      await uploadFile(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!account) {
      notify.warning('请先连接钱包');
      return;
    }

    if (!formData.name || !formData.description) {
      notify.warning('请填写作品名称和描述');
      return;
    }

    if (!formData.imageUrl) {
      notify.warning('请先上传作品文件');
      return;
    }

    setSubmitting(true);
    setShowProgress(true);

    try {
      // 创建铸造任务
      const token = localStorage.getItem('token');
      const response = await axios.post('/api/nft/mint-task/create', {
        creatorAddress: account,
        name: formData.name,
        description: formData.description,
        category: formData.category,
        imageUrl: formData.imageUrl,
        royaltyFee: formData.royaltyFee,
      }, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.data?.status === 'success') {
        setMintTask(response.data.data);
        notify.info('铸造任务已创建，正在处理...');
      } else {
        throw new Error(response.data?.message || '创建任务失败');
      }
    } catch (error) {
      console.error('铸造 NFT 失败:', error);
      notify.error(`铸造失败：${error.message}`);
      setSubmitting(false);
      setShowProgress(false);
    }
  };

  const getStepStatus = (stepIndex) => {
    if (!mintTask) {
      return stepIndex === 0 ? 'pending' : 'pending';
    }

    if (stepIndex < mintTask.currentStep - 1) {
      return 'completed';
    } else if (stepIndex === mintTask.currentStep - 1) {
      return mintTask.stepStatus;
    } else {
      return 'pending';
    }
  };

  const handleRetry = async () => {
    if (!mintTask) return;
    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(`/api/nft/mint-task/${mintTask.taskId}/retry`, {}, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.data?.status === 'success') {
        setMintTask(response.data.data);
        setSubmitting(true);
        notify.info('正在重试铸造...');
      }
    } catch (error) {
      console.error('重试失败:', error);
      notify.error(`重试失败：${error.response?.data?.message || error.message}`);
    }
  };

  const handleCancel = async () => {
    if (!mintTask) return;
    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(`/api/nft/mint-task/${mintTask.taskId}/cancel`, {}, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.data?.status === 'success') {
        setMintTask(response.data.data);
        setSubmitting(false);
        setShowProgress(false);
        notify.info('任务已取消');
      }
    } catch (error) {
      console.error('取消失败:', error);
      notify.error(`取消失败：${error.response?.data?.message || error.message}`);
    }
  };

  return (
    <Container>
      <Header
        currentPage="create"
        walletAddress={account}
        onWalletConnect={handleConnect}
        showLogout
      />

      <Content>
        <Title>创作 NFT</Title>

        {/* 步骤指示器 */}
        {showProgress && (
          <StepsContainer>
            <StepsTitle>铸造进度</StepsTitle>
            <StepsWrapper>
              {stepNames.map((name, index) => (
                <Step key={index}>
                  <StepCircle $status={getStepStatus(index)}>
                    {getStepStatus(index) === 'completed' ? '✓' :
                     getStepStatus(index) === 'failed' ? '✕' : index + 1}
                  </StepCircle>
                  <StepLabel $status={getStepStatus(index)}>{name}</StepLabel>
                </Step>
              ))}
            </StepsWrapper>

            {mintTask && (
              <ProgressContainer>
                <ProgressHeader>
                  <ProgressLabel>
                    {mintTask.failed ? '铸造失败' :
                     mintTask.completed ? '铸造完成' :
                     `正在${mintTask.stepName}...`}
                  </ProgressLabel>
                  <ProgressPercent>{mintTask.progressPercent}%</ProgressPercent>
                </ProgressHeader>
                <MainProgressBar>
                  <MainProgressFill progress={mintTask.progressPercent} />
                </MainProgressBar>

                {mintTask.failed && mintTask.errorMessage && (
                  <ErrorMessage>{mintTask.errorMessage}</ErrorMessage>
                )}

                {mintTask.failed && mintTask.canRetry && (
                  <ActionButtons>
                    <RetryButton onClick={handleRetry}>
                      重新铸造
                    </RetryButton>
                    <CancelButton onClick={handleCancel}>
                      取消任务
                    </CancelButton>
                  </ActionButtons>
                )}

                {mintTask.failed && mintTask.retryCount > 0 && (
                  <RetryInfo>
                    已重试 {mintTask.retryCount} 次，最多可重试 3 次
                  </RetryInfo>
                )}

                {mintTask.completed && (
                  <SuccessMessage>
                    🎉 恭喜！您的 NFT 已成功铸造，即将跳转到详情页...
                  </SuccessMessage>
                )}
              </ProgressContainer>
            )}
          </StepsContainer>
        )}

        <Form onSubmit={handleSubmit}>
          <FormGroup>
            <Label>作品文件</Label>
            <StorageOption>
              <StorageButton
                $active={storageType === 'minio'}
                onClick={() => {
                  setStorageType('minio');
                  notify.info('已切换到对象存储');
                }}
              >
                💾 对象存储 <MinioBadge>推荐</MinioBadge>
              </StorageButton>
              <StorageButton
                $active={storageType === 'ipfs'}
                onClick={() => {
                  setStorageType('ipfs');
                  notify.info('已切换到 IPFS 分布式存储');
                }}
              >
                🌐 IPFS <IpfsBadge>分布式</IpfsBadge>
              </StorageButton>
            </StorageOption>
            <UploadArea
              onClick={() => !isUploading && fileInputRef.current?.click()}
              onDragOver={(e) => e.preventDefault()}
              onDrop={handleDrop}
              style={{ opacity: isUploading ? 0.6 : 1, cursor: isUploading ? 'not-allowed' : 'pointer' }}
            >
              <UploadIcon>📁</UploadIcon>
              {previewUrl ? (
                <>
                  <FilePreview url={previewUrl} fileName={formData.name} />
                  {isUploading ? (
                    <>
                      <UploadProgress>
                        <ProgressBar progress={uploadProgress} />
                      </UploadProgress>
                      <ProgressText>上传中... {uploadProgress}%</ProgressText>
                    </>
                  ) : (
                    <>
                      <ImageActions>
                        <ActionButton onClick={() => fileInputRef.current?.click()}>
                          更换文件
                        </ActionButton>
                        <ActionButton onClick={() => {
                          setPreviewUrl(null);
                          setFileType(null);
                          setFormData(prev => ({ ...prev, imageUrl: '' }));
                        }}>
                          移除文件
                        </ActionButton>
                      </ImageActions>
                      <p style={{ marginTop: '12px', color: '#666', fontSize: '14px' }}>
                        点击更换或拖拽新文件
                      </p>
                    </>
                  )}
                </>
              ) : (
                <>
                  <p>点击或拖拽上传文件</p>
                  <Hint>
                    支持图片、PDF、视频、3D模型等任意格式，最大 100MB
                  </Hint>
                </>
              )}
            </UploadArea>
            <input
              ref={fileInputRef}
              type="file"
              accept="*/*"
              onChange={handleImageUpload}
              disabled={isUploading}
              style={{ display: 'none' }}
            />
          </FormGroup>

          <FormGroup>
            <Label>作品名称</Label>
            <Input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="输入作品名称"
              required
              disabled={submitting}
            />
          </FormGroup>

          <FormGroup>
            <Label>作品描述</Label>
            <Textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="描述你的作品..."
              required
              disabled={submitting}
            />
          </FormGroup>

          <FormGroup>
            <Label>分类</Label>
            <Select
              name="category"
              value={formData.category}
              onChange={handleChange}
              disabled={submitting}
            >
              {categories.map((cat) => (
                <option key={cat.value} value={cat.value}>
                  {cat.label}
                </option>
              ))}
            </Select>
          </FormGroup>

          <FormGroup>
            <Label>版税比例 (万分比)</Label>
            <Input
              type="number"
              name="royaltyFee"
              value={formData.royaltyFee}
              onChange={handleChange}
              min="0"
              max="10000"
              step="100"
              disabled={submitting}
            />
            <Hint>
              500 = 5%, 1000 = 10%。每次转售时你将获得相应比例的收益。
            </Hint>
          </FormGroup>

          <SubmitButton type="submit" disabled={submitting || !account}>
            {submitting ? '铸造中...' : !account ? '请先连接钱包' : '铸造 NFT'}
          </SubmitButton>
        </Form>
      </Content>
    </Container>
  );
}

export default CreatePage;