import axios from "axios";
import { useEffect, useState } from "react";
import { getUsers, type User } from "../services/api";

function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    async function loadUsers() {
      try {
        setIsLoading(true);
        setError(null);
        const data = await getUsers(controller.signal);
        setUsers(data);
      } catch (requestError) {
        if (!axios.isCancel(requestError)) {
          setError("Unable to load users. Check that the API Gateway is running.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    void loadUsers();

    return () => controller.abort();
  }, []);

  return (
    <section>
      <div className="page-heading">
        <div>
          <div className="eyebrow">Community</div>
          <h1>Users</h1>
        </div>
        {!isLoading && !error && (
          <span className="result-count">
            {users.length} {users.length === 1 ? "user" : "users"}
          </span>
        )}
      </div>

      {isLoading && <div className="status-panel">Loading users...</div>}

      {error && (
        <div className="status-panel status-error" role="alert">
          {error}
        </div>
      )}

      {!isLoading && !error && users.length === 0 && (
        <div className="status-panel">No users have been added yet.</div>
      )}

      {!isLoading && !error && users.length > 0 && (
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td className="muted-cell">#{user.id}</td>
                  <td className="strong-cell">{user.name}</td>
                  <td>{user.email}</td>
                  <td>{new Date(user.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

export default UsersPage;
