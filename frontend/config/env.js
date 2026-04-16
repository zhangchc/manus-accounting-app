/**
 * 前端环境配置
 * - development -> test
 * - production  -> prod
 */
// const ENV_MODE = process.env.NODE_ENV === 'production' ? 'prod' : 'test';
const ENV_MODE = 'prod';

const ENV_CONFIG = {
  test: {
    name: 'test',
    baseUrl: 'http://localhost:9091/api'
  },
  prod: {
    name: 'prod',
    baseUrl: 'https://jizhang.life365.xin/api'
  }
};

const currentEnv = ENV_CONFIG[ENV_MODE] || ENV_CONFIG.test;

export { ENV_MODE, ENV_CONFIG, currentEnv };
