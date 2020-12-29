-- --------------------------------------------------------------------
-- 商户模型新增主子商户关系
-- --------------------------------------------------------------------
ALTER TABLE `upay_merchant` ADD COLUMN `parent_id` BIGINT NOT NULL COMMENT '父商户ID' AFTER `name`;
UPDATE `upay_merchant` SET `parent_id` = 0;

-- 新增移动端应用
DELETE FROM `upay_application` WHERE `app_id` = 2010;
INSERT INTO `upay_application`(`app_id`, `mch_id`, `name`, `access_token`, `app_private_key`, `app_public_key`, `private_key`, `public_key`, `created_time`)
VALUES (2010, 0, '移动应用', 'abcd2010', 'MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEA1kbf57+InuWrVukfg/uw9QdCMwZ57KlDJa7+TyfrayK3aNIQ2MuknAbc+8M8Np/DlQfa+GMrShXvQeES0r7W+QIDAQABAkBTBULbV6pnZjTsh4ZebLYzOYy8mFXFDA+oGhUONjlQWH1IK5AYYzVbrc6+mVZLi4z1/EW56BSPhYrXZrP2lorBAiEA+IMCdOdK4k12ygzR0UQBVA1QlCXk4T6XIwO4yhCa+UMCIQDcu8fxuIbpWvOoqndBml0E42iZrHI01iky3gAqIvtdEwIgLz1oOCTHfWFQVXQ+ZlNRFVM6oA7cBV1KiaNpey/Q5dUCIB4LEPO9gd9RGcjjKsgrEm4P5bTE2+aFH6ZkwPD7QesxAiBEV7L4LWTPkjsYvC2wdEITi7WwO6dcAaAfpjaiFAOKyg==', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANZG3+e/iJ7lq1bpH4P7sPUHQjMGeeypQyWu/k8n62sit2jSENjLpJwG3PvDPDafw5UH2vhjK0oV70HhEtK+1vkCAwEAAQ==',
        'MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAkdzZD5Mvnde9pAfIMYf19O6j9v0yMtFCxyIKWS1UvAb4z33iOyaxomYYMc2goTfHTvqVsOhpyhzGSQsTbD8w/QIDAQABAkAUOOgnDqLlYUm7ehC5PT5OTN+SmJvjC7wUW5XPs0cyIhYGiWMlLFhLAf9Sth4fzL3ixDecSSnctIIh3Vf188aJAiEA+JVABJkB9plHCYPPNMQXen9wCJ1wPXV1+vNwCbaXRE8CIQCWNv1o6nSYHdmf0YKm+kqSXGoVJe45IDw2Rum1QpiG8wIhAIrDhgELCLWHysfc9IYYEKMpEHk+qbElKL71tc02SCqxAiAYKuC6cH4xuxu4SszqcHpu8c9fd6rMJhOJ5/7R2tUPYQIgN8nR/F7oNeRhX3LI6lmBmO/uYhfGWn4dNlMNyUPC05U=', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJHc2Q+TL53XvaQHyDGH9fTuo/b9MjLRQsciClktVLwG+M994jsmsaJmGDHNoKE3x076lbDoacocxkkLE2w/MP0CAwEAAQ==', now());