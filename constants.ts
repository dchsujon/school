// FIX: Replaced placeholder content with actual constant definitions for class configurations and months, which are used throughout the application.
export interface ClassConfig {
  subjects: string[];
  maxMarks: Record<string, number>;
  passMarkPercentage: number;
  subjectCodes: Record<string, string>;
  tuitionFee: number;
}

export const CLASS_CONFIGS: Record<string, ClassConfig> = {
  'Class One': {
    subjects: ['bangla', 'english', 'mathematics'],
    maxMarks: { 'bangla': 100, 'english': 100, 'mathematics': 100 },
    passMarkPercentage: 40,
    subjectCodes: { 'bangla': '101', 'english': '102', 'mathematics': '103' },
    tuitionFee: 1200,
  },
  'Class Two': {
    subjects: ['bangla', 'english', 'mathematics', 'general knowledge'],
    maxMarks: { 'bangla': 100, 'english': 100, 'mathematics': 100, 'general knowledge': 50 },
    passMarkPercentage: 40,
    subjectCodes: { 'bangla': '201', 'english': '202', 'mathematics': '203', 'general knowledge': '204' },
    tuitionFee: 1300,
  },
  'Class Three': {
    subjects: ['bangla', 'english', 'mathematics', 'social science', 'general science'],
    maxMarks: { 'bangla': 100, 'english': 100, 'mathematics': 100, 'social science': 100, 'general science': 100 },
    passMarkPercentage: 40,
    subjectCodes: { 'bangla': '301', 'english': '302', 'mathematics': '303', 'social science': '304', 'general science': '305' },
    tuitionFee: 1400,
  },
  'Class Four': {
    subjects: ['bangla', 'english', 'mathematics', 'social science', 'general science', 'religion'],
    maxMarks: { 'bangla': 100, 'english': 100, 'mathematics': 100, 'social science': 100, 'general science': 100, 'religion': 100 },
    passMarkPercentage: 40,
    subjectCodes: { 'bangla': '401', 'english': '402', 'mathematics': '403', 'social science': '404', 'general science': '405', 'religion': '406' },
    tuitionFee: 1500,
  },
  'Class Five': {
    subjects: ['bangla', 'english', 'mathematics', 'social science', 'general science', 'religion'],
    maxMarks: { 'bangla': 100, 'english': 100, 'mathematics': 100, 'social science': 100, 'general science': 100, 'religion': 100 },
    passMarkPercentage: 40,
    subjectCodes: { 'bangla': '501', 'english': '502', 'mathematics': '503', 'social science': '504', 'general science': '505', 'religion': '506' },
    tuitionFee: 1600,
  },
  'Class Six': {
    subjects: ['bangla', 'english', 'mathematics', 'social science', 'general science', 'religion', 'ict'],
    maxMarks: { 'bangla': 100, 'english': 100, 'mathematics': 100, 'social science': 100, 'general science': 100, 'religion': 100, 'ict': 50 },
    passMarkPercentage: 40,
    subjectCodes: { 'bangla': '601', 'english': '602', 'mathematics': '603', 'social science': '604', 'general science': '605', 'religion': '606', 'ict': '607' },
    tuitionFee: 1800,
  },
   'Class Seven': {
    subjects: ['bangla', 'english', 'mathematics', 'social science', 'general science', 'religion', 'ict'],
    maxMarks: { 'bangla': 100, 'english': 100, 'mathematics': 100, 'social science': 100, 'general science': 100, 'religion': 100, 'ict': 50 },
    passMarkPercentage: 40,
    subjectCodes: { 'bangla': '701', 'english': '702', 'mathematics': '703', 'social science': '704', 'general science': '705', 'religion': '706', 'ict': '707' },
    tuitionFee: 1900,
  },
   'Class Eight': {
    subjects: ['bangla', 'english', 'mathematics', 'social science', 'general science', 'religion', 'ict'],
    maxMarks: { 'bangla': 100, 'english': 100, 'mathematics': 100, 'social science': 100, 'general science': 100, 'religion': 100, 'ict': 50 },
    passMarkPercentage: 40,
    subjectCodes: { 'bangla': '801', 'english': '802', 'mathematics': '803', 'social science': '804', 'general science': '805', 'religion': '806', 'ict': '807' },
    tuitionFee: 2000,
  },
  'default': {
    subjects: [],
    maxMarks: {},
    passMarkPercentage: 40,
    subjectCodes: {},
    tuitionFee: 1000,
  }
};

export const getConfigForClass = (className: string): ClassConfig => {
  return CLASS_CONFIGS[className] || CLASS_CONFIGS['default'];
};

export const MONTHS = [
    'January', 'February', 'March', 'April', 'May', 'June', 
    'July', 'August', 'September', 'October', 'November', 'December'
];