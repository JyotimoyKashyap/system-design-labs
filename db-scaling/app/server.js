const express = require('express');
const mysql = require('mysql2');
const app = express();


// Pool for Users 1-10
const shard1 = mysql.createPool({
  host: 'mysql-shard-1',
  user: 'root',
  password: 'root_password',
  database: 'shard_1'
});

// Pool for Users 11-20
const shard2 = mysql.createPool({
  host: 'mysql-shard-2',
  user: 'root',
  password: 'root_password',
  database: 'shard_2'
});

const getShardPool = (userId) => {
    if (userId >= 1 && userId <= 10) return shard1;
    if (userId >= 11 && userId <= 20) return shard2;
    throw new Error('User ID out of range');
};


app.get('/user/:id', (req, res) => {
    try {
        const userId = parseInt(req.params.id);
        const db = getShardPool(userId);
        const shardName = userId <= 10 ? 'SHARD_1' : 'SHARD_2';

        db.query('SELECT * FROM users WHERE id = ?', [userId], (err, results) => {
            if (err) return res.status(500).json({ success: false, message: `Database Error : ${err.message}` });
            
            if (results.length === 0) {
                res.status(404).json({
                    source: shardName,
                    success: false,
                    message: 'User not found'
                });
                return;
            }

            res.json({
                source: shardName,
                success: true,
                data: results[0]
            });
        });

    } catch (err) {
        console.error(`Routing Error : ${err.message}`);
        res.status(400).json({ 
            success: false,
            message: err.message
        });
    }
});

app.post('/write', (req, res) => {
    writeDb.query('INSERT INTO users (name) VALUES ("New User")', (err, results) => {
        res.json({ source: 'MASTER', data: results });
    });
});

app.listen(3000, () => { console.log('Server running on port 3000'); });