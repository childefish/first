package com.csdn.demo.sys.service;


import com.csdn.demo.common.base.dao.GenericDao;
import com.csdn.demo.common.base.entity.Page;
import com.csdn.demo.common.base.service.GenericService;
import com.csdn.demo.common.util.user.UserInfo;
import com.csdn.demo.sys.dao.UserAssociateRoleDao;
import com.csdn.demo.sys.dao.UserDao;
import com.csdn.demo.sys.entity.QueryUser;
import com.csdn.demo.sys.entity.User;
import com.csdn.demo.sys.entity.UserAssociateRole;
import com.csdn.demo.sys.entity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 *@author linzf
 **/
@Service("userService")
@Transactional(rollbackFor={IllegalArgumentException.class})
public class UserService extends GenericService<User, QueryUser> {
	@Autowired
	@SuppressWarnings("SpringJavaAutowiringInspection")
	private UserDao userDao;

	@Inject
	@SuppressWarnings("SpringJavaAutowiringInspection")
	private UserAssociateRoleDao userAssociateRoleDao;

	@Override
	protected GenericDao<User, QueryUser> getDao() {
		return userDao;
	}

	/**
	 * 分页查询组织架构底下的用户
	 * @param queryUser 查询条件
	 *  */
	public Page findByGroupUserPage(QueryUser queryUser){
		List<User> list =  userDao.findGroupUserByPage(queryUser);
		int count = userDao.countGroupUser(queryUser);
		return new Page(list, count);
	}

	/**
	 * 功能描述：实现增加用户
	 * @param entity 保存对象
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean save(User entity) throws Exception {
		entity.setAddress(entity.getProvince()+entity.getCity()+entity.getDistrict()+entity.getStreetAddress());
		entity.setPassword(UserInfo.encode(entity.getPassword()));
		entity.setState("1");
		entity.packagingRoles(entity.getRoleArray());
		List<UserRole> userRoleList = entity.getRoles();
		boolean success = userDao.save(entity)>0;
		if(success){
			if(userRoleList.size()>0){
				for(UserRole userRole:userRoleList){
					userAssociateRoleDao.save(new UserAssociateRole(entity.getId(),userRole.getId()));
				}
			}
		}
		return success;
	}

	/**
	 * 功能描述：实现更新用户
	 * @param entity 修改对象
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean update(User entity) throws Exception {
		entity.packagingRoles(entity.getRoleArray());
		entity.setAddress(entity.getProvince()+entity.getCity()+entity.getDistrict()+entity.getStreetAddress());
		userAssociateRoleDao.removeUserRole(entity);
		if(entity.getRoles().size()>0){
			for(UserRole userRole:entity.getRoles()){
				userAssociateRoleDao.save(new UserAssociateRole(entity.getId(),userRole.getId()));
			}
		}
		return super.update(entity);
	}

	/**
	 * 功能描述：批量删除用户
	 * @param entityList
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean removeBath(List<User> entityList) throws Exception {
		for(User user:entityList){
			userAssociateRoleDao.removeUserRole(user);
		}
		return super.removeBath(entityList);
	}

	/**
	 * 功能描述：更新用户状态为可用或者不可用
	 * @param user
	 * @return
	 */
	public boolean userControl(User user){
		return userDao.userControl(user)>0;
	}

	/**
	 * 功能描述：根据账号来获取用户信息
	 * @param login
	 * @return
	 */
	public User findByLogin(String login){
		return userDao.findByLogin(login);
	}

}