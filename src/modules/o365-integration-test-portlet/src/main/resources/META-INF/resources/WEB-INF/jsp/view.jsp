<%--
  ~ Copyright (c) 2019 Savoir-faire Linux Inc.
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU LesserGeneral Public License as published by
  ~ the Free Software Foundation, either version 2.1 of the License, or
  ~ (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU LesserGeneral Public License for more details.
  ~
  ~ You should have received a copy of the GNU LesserGeneral Public License
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ include file="init.jsp"%>

<portlet:actionURL name="removeAuth" var="removeAuth"/>
<portlet:actionURL name="removeSession" var="removeSession"/>
<portlet:actionURL name="expireSession" var="expireSession"/>
<portlet:actionURL name="removeProperties" var="removeProperties"/>
<portlet:actionURL name="createEvent" var="createEvent"/>
<c:choose>
	<c:when test="${isConnected}">
		<pre>
		    unread mail: ${unreadMail}
		    next event time: ${nextEventTime}
		</pre>
		<a href="${removeAuth}" class="btn btn-danger">Delete Auth</a>
		<a href="${removeSession}" class="btn btn-danger">Delete session</a>
		<a href="${expireSession}" class="btn btn-warning">Expire session</a>
		<a href="${removeProperties}" class="btn btn-danger">Delete properties</a>
		<br/>
		<a href="${createEvent}" class="btn btn-primary">Create event</a>

	</c:when>
	<c:otherwise>
		<a href="/o/o365/login?backURL=${backURL}" class="btn btn-primary">Connect to Office365</a>
	</c:otherwise>
</c:choose>

<c:forEach var="user" items="${users}">
    ${user} <br />
</c:forEach>
